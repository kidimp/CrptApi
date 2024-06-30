import com.selsup.CrptApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class CrptApiTest {
    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 5);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            CrptApi.Document document = crptApi.new Document();
            document.doc_id = "123";
            document.doc_status = "NEW";
            document.doc_type = "TYPE";
            document.importRequest = false;
            document.owner_inn = "1234567890";
            document.participant_inn = "0987654321";
            document.producer_inn = "1122334455";
            document.production_date = "2024-06-29";
            document.production_type = "TYPE";
            document.products = new CrptApi.Product[0];
            document.reg_date = "2024-06-29";
            document.reg_number = "REG123";

            crptApi.createDocument(document, "my-signature");

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            System.out.println("Document created at: " + now.format(formatter));
        };

        scheduler.scheduleAtFixedRate(task, 0, 5, TimeUnit.SECONDS);

        try {
            Thread.sleep(60_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }

        System.out.println("Scheduler has completed.");

        while (!crptApi.requestQueue.isEmpty()) {

        }

        System.out.println("Test completed.");
    }
}