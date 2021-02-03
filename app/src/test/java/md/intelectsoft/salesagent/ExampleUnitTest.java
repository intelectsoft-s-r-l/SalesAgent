package md.intelectsoft.salesagent;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }


    @Test
    public void verif () {
        String url = getApiOrderService("http://192.168.1.181:5965/ISRemoteOrder/");
        assertEquals(url, "http://192.168.1.181:5965/ISRemoteOrder/");
    }

    public String getApiOrderService(String url_OrderService){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url_OrderService)
                .addConverterFactory(GsonConverterFactory.create())
                .client(getOkHttpClient())
                .build();

        return retrofit.baseUrl().toString();
    }

    private OkHttpClient getOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(8, TimeUnit.SECONDS)
                .build();
    }


    @Test
    public void checkFormatDate(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Chisinau");
        simpleDateFormat.setTimeZone(timeZone);

        String dateValid = "/Date(1611738951648+0200)/";
        if (dateValid != null) {
            if (dateValid != null)
                dateValid = dateValid.replace("/Date(", "");
            if (dateValid != null)
                dateValid = dateValid.substring(0, dateValid.length() - 7);
        }
        long timeValid = Long.parseLong(dateValid);
        String date = simpleDateFormat.format(timeValid);

        assertNotEquals(date, null);
    }
}