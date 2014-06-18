package controllers;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import controllers.util.exceptions.JsonConverterException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jdeferred.DoneFilter;
import org.jdeferred.Promise;
import util.APIUtils;
import util.ToCSV;
import util.json.JSONObject;
import util.json.XML;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by Paulo on 10/06/14.
 */
public class JsonConverter {
    public static Promise<InputStream, Throwable, Double> xml2json(Promise<InputStream, Throwable, Double> resource) {
        return resource.then(new DoneFilter<InputStream, InputStream>() {
            @Override
            public InputStream filterDone(InputStream stream) {
                try {
                    JSONObject jsonObject = XML.toJSONObject(APIUtils.convertStreamToString(stream));
                    String s = jsonObject.toString();
                    return new ByteArrayInputStream(s.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new JsonConverterException("Failed while converting XML to JSON", e);
                }
            }
        });
    }

    public static Promise<InputStream, Throwable, Double> csv2json(Promise<InputStream, Throwable, Double> resource) {
        return resource.then(new DoneFilter<InputStream, InputStream>() {
            @Override
            public InputStream filterDone(InputStream stream) {
                return csvToJson(stream);
            }
        });
    }

    public static Promise<InputStream, Throwable, Double> xls2json(Promise<InputStream, Throwable, Double> resource) {
        return resource.then(new DoneFilter<InputStream, InputStream>() {
            @Override
            public InputStream filterDone(InputStream stream) {
                ToCSV toCSV = new ToCSV();
                try {
                    InputStream inputStream = toCSV.convertToCSV(stream);
                    inputStream = csvToJson(inputStream);
                    return inputStream;
                } catch (IOException e) {
                    throw new JsonConverterException("Failed while converting XLS to JSON", e);
                } catch (InvalidFormatException e) {
                    throw new JsonConverterException("Failed while converting XLS to JSON", e);
                }

            }
        });
    }

    private static InputStream csvToJson(InputStream stream){
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        try {
            MappingIterator<Object> objectMappingIterator = csvMapper.reader(Object.class).with(schema).readValues(stream);
            List<Object> objects = objectMappingIterator.readAll();
            ObjectMapper mapper = new ObjectMapper();
            String s = mapper.writeValueAsString(objects);
            return new ByteArrayInputStream(s.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new JsonConverterException("Failed while converting CSV to JSON", e);
        }
    }
}
