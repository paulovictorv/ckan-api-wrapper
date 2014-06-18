package util.converters;

import controllers.JsonConverter;
import org.jdeferred.DoneCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.junit.BeforeClass;
import org.junit.Test;
import util.APIUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by Paulo on 17/06/14.
 */
public class JsonConverterTest {

    private static InputStream jsonStream;
    private static InputStream xmlStream;
    private static InputStream csvStream;
    private static InputStream xlsStream;

    private static String json;
    private static String json2csv;
    private static String xls2json;


    @BeforeClass
    public static void init() throws FileNotFoundException {
        jsonStream = new FileInputStream(new File("res/test.json"));
        String json = APIUtils.convertStreamToString(jsonStream);
        JsonConverterTest.json = json;

        FileInputStream j2cStream = new FileInputStream("res/csv2json.json");
        json2csv = APIUtils.convertStreamToString(j2cStream);
        FileInputStream x2jStream = new FileInputStream("res/xls2json.json");
        xls2json = APIUtils.convertStreamToString(x2jStream);

        xmlStream = new FileInputStream(new File("res/test.xml"));

        csvStream = new FileInputStream(new File("res/test.csv"));

        xlsStream = new FileInputStream(new File("res/teste-xls.xls"));
    }

    private Promise<InputStream, Throwable, Double> obtainFakePromise(InputStream stream){
        DeferredObject<InputStream, Throwable, Double> deferredObject = new DeferredObject<>();
        deferredObject.resolve(stream);
        return deferredObject.promise();
    }

    @Test
    public void shouldConvertXml2Json() throws InterruptedException {

        Promise<InputStream, Throwable, Double> promise = JsonConverter.xml2json(obtainFakePromise(xmlStream));

        promise.done(new DoneCallback<InputStream>() {
            @Override
            public void onDone(InputStream stream) {
                String s = APIUtils.convertStreamToString(stream);
                assertThat(s).isEqualTo(json);
            }
        });

        promise.waitSafely();
    }

    @Test
    public void shouldConvertCsv2Json() throws InterruptedException {
        Promise<InputStream, Throwable, Double> promise = JsonConverter.csv2json(obtainFakePromise(csvStream));

        promise.done(new DoneCallback<InputStream>() {
            @Override
            public void onDone(InputStream stream) {
                String s = APIUtils.convertStreamToString(stream);
                assertThat(s).isEqualTo(json2csv);
            }
        });

        promise.waitSafely();
    }

    @Test
    public void shouldConvertXls2Json() throws InterruptedException {
        Promise<InputStream, Throwable, Double> promise = JsonConverter.xls2json(obtainFakePromise(xlsStream));

        promise.done(new DoneCallback<InputStream>() {
            @Override
            public void onDone(InputStream stream) {
                String s = APIUtils.convertStreamToString(stream);
                assertThat(s).isEqualTo(xls2json);
            }
        });

        promise.waitSafely();
    }

}
