package com.selsup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.selsup.exceptions.CreateDocumentException;
import com.selsup.exceptions.SendRequestException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class CrptApi {
    private final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private RateLimiter rateLimiter;
    public final BlockingQueue<HttpRequest> requestQueue;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        rateLimiter = new RateLimiter(timeUnit, requestLimit);
        requestQueue = new LinkedBlockingQueue<>();

        CompletableFuture.runAsync(() -> {
            while (true) {
                if (!requestQueue.isEmpty()) {
                    if (rateLimiter.tryAcquire()) {
                        try {
                            sendRequest(requestQueue.take());
                        } catch (InterruptedException | IOException e) {
                            throw new SendRequestException("Error while sending request", e);
                        }
                    }
                }
            }
        });
    }

    private HttpRequest createRequest(Document document, String signature) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(document);

        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Signature", signature)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private void sendRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().build();
        client.send(request, HttpResponse.BodyHandlers.discarding());
    }

    public void createDocument(Document document, String signature) {
        CompletableFuture.runAsync(() -> {
            try {
                requestQueue.put(createRequest(document, signature));
            } catch (InterruptedException | JsonProcessingException e) {
                throw new CreateDocumentException("Error while creating document request", e);
            }
        });
    }

    public class RateLimiter {
        private long lastRequestTime;
        private AtomicInteger requestCount;
        private final int requestLimit;
        private final long timeSize;

        RateLimiter(TimeUnit timeUnit, int requestLimit) {
            requestCount = new AtomicInteger(0);
            this.requestLimit = requestLimit;
            timeSize = timeUnit.toMillis(1);
        }

        public synchronized boolean tryAcquire() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRequestTime > timeSize) {
                requestCount = new AtomicInteger(0);
                lastRequestTime = currentTime;
            }

            if (requestCount.intValue() < requestLimit) {
                requestCount.incrementAndGet();
                return true;
            } else {
                return false;
            }
        }
    }


// Названия полей классов взяты из JSON-примера из задания.
    public class Document {
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public Product[] products;
        public String reg_date;
        public String reg_number;
    }

    public class Description {
        public String participantInn;
    }

    public class Product {
        public String certificate_document;
        public String certificate_document_date;
        public String certificate_document_number;
        public String owner_inn;
        public String producer_inn;
        public String production_date;
        public String tnved_code;
        public String uit_code;
        public String uitu_code;
    }
}