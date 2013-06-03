/*
#
# Copyright 2013 The Trustees of Indiana University
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# -----------------------------------------------------------------
#
# Project: data-api
# File:  SimpleTokenizer.java
# Description:  
#
# -----------------------------------------------------------------
# 
*/



/**
 * 
 */
package edu.indiana.d2i.htrc.access.tokencount;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import edu.indiana.d2i.htrc.access.ParameterContainer;
import edu.indiana.d2i.htrc.access.VolumeReader;
import edu.indiana.d2i.htrc.access.VolumeReader.ContentReader;
import edu.indiana.d2i.htrc.access.VolumeRetriever;
import edu.indiana.d2i.htrc.access.exception.DataAPIException;

/**
 * @author Yiming Sun
 *
 */
public class SimpleTokenizer implements Tokenizer {
    
    
    private static Logger log = Logger.getLogger(SimpleTokenizer.class);
    
    static class TokenizationCallable implements Callable<TokenPackage> {
        protected final String volumeID;
        protected final ContentReader contentReader;
        
        TokenizationCallable(String volumeID, ContentReader contentReader) {
            this.volumeID = volumeID;
            this.contentReader = contentReader;
        }

        /**
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public TokenPackage call() throws Exception {
            byte[] content = contentReader.getContent();
            InputStream inputStream = new ByteArrayInputStream(content);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            String hangingWord = null;
            List<String> tokenList = new LinkedList<String>();
            if (log.isDebugEnabled()) log.debug("tokenizing page content");
            do {
                line = reader.readLine();
                if (line != null) {
                    StringTokenizer tokenizer = new StringTokenizer(line);
                    int tokenCount = tokenizer.countTokens();
                    if (tokenCount > 0) {
                        String token = tokenizer.nextToken().trim();
                        if (hangingWord != null) {
                            tokenList.add(hangingWord + token);
                            hangingWord = null;
                        } else {
                            tokenList.add(token);
                        }
                    }
                    
                    for (int i = 1; i < tokenCount - 1; i++) {
                        String token = tokenizer.nextToken().trim();
                        tokenList.add(token);
                    }
                    
                    if (tokenCount > 1) {
                        String token = tokenizer.nextToken().trim();
                        if (token.endsWith(HYPHEN)) {
                            hangingWord = token;
                        } else {
                            tokenList.add(token);
                        }
                    }
                }
            } while (line != null);
            reader.close();
            
            if (hangingWord != null) {
                tokenList.add(hangingWord);
            }
            
            ContentIdentifier contentIdentifier = new ContentIdentifierImpl(volumeID, contentReader.getContentName());
            TokenPackage tokenPackage = new SimpleTokenPackageImpl(contentIdentifier, tokenList);
            if (log.isDebugEnabled()) log.debug("built TokenPackage for " + volumeID + " " + contentReader.getContentName());
            return tokenPackage;
        }
        
    }

    static class ThrottledTokenPackageIterator implements Iterator<TokenPackage> {
    
        private static final Logger log = Logger.getLogger(ThrottledTokenPackageIterator.class);
        static final String PN_MAX_TOKENIZATION_TASKS = "max.tokenization.tasks";
        static final String PN_MIN_TOKENIZATION_TASKS = "min.tokenization.tasks";
        
        protected final VolumeRetriever volumeRetriever;
        protected final ExecutorService executorService;
        protected final List<Future<TokenPackage>> tokenPackageList;
        protected final List<DataAPIException> exceptionList;
        protected final int maxTokenizationTasks;
        protected final int minTokenizationTasks;
        protected VolumeReader currentVolumeReader;
        
        ThrottledTokenPackageIterator(VolumeRetriever volumeRetriever, ParameterContainer parameterContainer, ExecutorService executorService) {
            this.volumeRetriever = volumeRetriever;
            this.executorService = executorService;
            this.tokenPackageList = new LinkedList<Future<TokenPackage>>();
            this.exceptionList = new LinkedList<DataAPIException>();
            this.maxTokenizationTasks = Integer.parseInt(parameterContainer.getParameter(PN_MAX_TOKENIZATION_TASKS));
            this.minTokenizationTasks = Integer.parseInt(parameterContainer.getParameter(PN_MIN_TOKENIZATION_TASKS));
            this.currentVolumeReader = null;
        }
        
        
        /**
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            boolean hasNext = (currentVolumeReader != null && currentVolumeReader.hasMorePages()) || volumeRetriever.hasMoreVolumes() || !tokenPackageList.isEmpty() || !exceptionList.isEmpty();
            
            return hasNext;
        }
        /**
         * @see java.util.Iterator#next()
         */
        @Override
        public TokenPackage next() {
            TokenPackage tokenPackage = null;
            boolean done = false;
            
            while (!done) {
                if (!tokenPackageList.isEmpty()) {
                    Future<TokenPackage> future = tokenPackageList.remove(0);
                    try {
                        
                        tokenPackage = future.get();
                        done = true;
                    } catch (InterruptedException e) {
                        log.error("Async tokenization interrupted:", e);
                    } catch (ExecutionException e) {
                        log.error("future.get() caused exception", e);
                        Throwable throwable = e.getCause();
                        if (throwable instanceof DataAPIException) {
                            exceptionList.add((DataAPIException)throwable);
                        }
                    }
                    
                    if (tokenPackageList.size() <= minTokenizationTasks) {
                        if (log.isDebugEnabled()) log.debug("trigger threshold reached");
                        dispatchWork();
                    }
                } else if (currentVolumeReader != null && currentVolumeReader.hasMorePages()) {
                    if (log.isDebugEnabled()) log.debug("tokenPackageList empty, dispatch more work using half-eaten volumeReader");
                    dispatchWork();
                } else if (volumeRetriever.hasMoreVolumes()) {
                    if (log.isDebugEnabled()) log.debug("tokenPackageList empty, dispatch more work from VolumeRetriever");
                    dispatchWork();
                } else if (!exceptionList.isEmpty()) {
                    if (log.isDebugEnabled()) log.debug("only exceptions are left");
                    DataAPIException dataAPIException = exceptionList.remove(0);
                    tokenPackage = new ExceptionTokenPackage(dataAPIException);
                    done = true;
                } else {
                    if (log.isDebugEnabled()) log.debug("nothing more to tokenize. all done");
                    done = true;
                }
            }

            return tokenPackage;
        }
        
        protected void dispatchWork() {
            int availableSlots = maxTokenizationTasks - tokenPackageList.size();
            
            boolean done = false;
            while (availableSlots > 0 && !done) {
                if (currentVolumeReader != null && currentVolumeReader.hasMorePages()) {
                    ContentReader nextPage = currentVolumeReader.nextPage();
                    TokenizationCallable tokenizationCallable = new TokenizationCallable(currentVolumeReader.getVolumeID(), nextPage);
                    Future<TokenPackage> future = executorService.submit(tokenizationCallable);
                    tokenPackageList.add(future);
                    availableSlots--;
                } else if (volumeRetriever.hasMoreVolumes()) {
                    try {
                        currentVolumeReader = volumeRetriever.nextVolume();
                    } catch (DataAPIException e) {
                        exceptionList.add(e);
                    }
                    
                } else {
                    if (log.isDebugEnabled()) log.debug("no more volumes to be tokenized");
                    done = true;
                }
            }
            
        }
        /**
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            // TODO Auto-generated method stub
            
        }
        
        
    }
    
    
//    static class TokenPackageIterator implements Iterator<TokenPackage> {
//
//        private static final Logger log = Logger.getLogger(TokenPackageIterator.class);
//        
//        protected final List<Future<TokenPackage>> tokenPackageList;
//        protected final List<DataAPIException> exceptionList;
//        
//        
//        TokenPackageIterator(List<Future<TokenPackage>> tokenPackageList, List<DataAPIException> exceptionList) {
//            this.tokenPackageList = tokenPackageList;
//            this.exceptionList = exceptionList;
//        }
//        /**
//         * @see java.util.Iterator#hasNext()
//         */
//        @Override
//        public boolean hasNext() {
//            return (!tokenPackageList.isEmpty() || !exceptionList.isEmpty());
//        }
//
//        /**
//         * @see java.util.Iterator#next()
//         */
//        @Override
//        public TokenPackage next() {
//            TokenPackage tokenPackage = null;
//            if (!tokenPackageList.isEmpty()) {
//                Future<TokenPackage> future = tokenPackageList.remove(0);
//                try {
//                    tokenPackage = future.get();
//                } catch (ExecutionException e) {
//                    log.error("ExecutionException", e);
//                } catch (InterruptedException e) {
//                    log.error("InterruptedException", e);
//                }
//            } else if (!exceptionList.isEmpty()) {
//                DataAPIException dataAPIException = exceptionList.remove(0);
//                tokenPackage = new ExceptionTokenPackage(dataAPIException);
//            }
//            return tokenPackage;
//        }
//
//        /**
//         * @see java.util.Iterator#remove()
//         */
//        @Override
//        public void remove() {
//            
//        }
//        
//    }
    protected final ExecutorService executorService;
    protected final ParameterContainer parameterContainer;
    public SimpleTokenizer(ExecutorService executorService, ParameterContainer parameterContainer) {
        this.executorService = executorService;
        this.parameterContainer = parameterContainer;
    }
    
    /**
     * @see edu.indiana.d2i.htrc.access.tokencount.Tokenizer#tokenize(edu.indiana.d2i.htrc.access.VolumeRetriever)
     */
    @Override
    public Iterator<TokenPackage> tokenize(VolumeRetriever volumeRetriever) {
//        List<Future<TokenPackage>> futureList = new LinkedList<Future<TokenPackage>>();
//        List<DataAPIException> exceptionList = new LinkedList<DataAPIException>();

        Iterator<TokenPackage> iterator = new ThrottledTokenPackageIterator(volumeRetriever, parameterContainer, executorService);
//        while (volumeRetriever.hasMoreVolumes()) {
//            try {
//                VolumeReader volumeReader = volumeRetriever.nextVolume();
//                while (volumeReader.hasMorePages()) {
//                    ContentReader contentReader = volumeReader.nextPage();
//                    TokenizationCallable tokenizationCallable = new TokenizationCallable(volumeReader.getVolumeID(), contentReader);
//                    Future<TokenPackage> future = executorService.submit(tokenizationCallable);
//                    futureList.add(future);
//                }
//            } catch (KeyNotFoundException knfe) { 
//                exceptionList.add(knfe);
//            } catch (PolicyViolationException e) {
//                exceptionList.add(e);
//            } catch (RepositoryException e) {
//                exceptionList.add(e);
//            }
//        }
//        
//        Iterator<TokenPackage> iterator = new TokenPackageIterator(futureList, exceptionList);
//        if (log.isDebugEnabled()) log.debug("returning token package iterator");
        return iterator;
    }

}
