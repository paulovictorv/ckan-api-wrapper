package controllers;

import br.wrapper.ckanclient.model.CKANResource;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Paulo on 17/06/14.
 */
public class PlotRepository {

    private static Map<String, String> plotterMap;

    static {
        plotterMap = new HashMap<>();
        plotterMap.put("05a7a012-2762-41d8-83cd-f51b9b3a3e4a", "{\"url\": \"https://plot.ly/~paulomelo/1\", \"message\": \"\", \"warning\": \"\", \"filename\": \"ideb-total-8-serie-do-ensino-fundamental\", \"error\": \"\"}");
        plotterMap.put("06adef99-648e-4284-b6ad-b8d5f1715b67", "{\"url\": \"https://plot.ly/~paulomelo/2\", \"message\": \"\", \"warning\": \"\", \"filename\": \"ideb-total-8-serie-do-ensino-fundamental\", \"error\": \"\"}");
        plotterMap.put("000a8189-cad0-4ff9-990b-24c2344486d6", "{\"url\": \"https://plot.ly/~paulomelo/3\", \"message\": \"\", \"warning\": \"\", \"filename\": \"ideb-total-8-serie-do-ensino-fundamental\", \"error\": \"\"}");
    }

    public static String obtainPlot(CKANResource dataset) {
        String plotInfo = plotterMap.get(dataset.packageId);
        return plotInfo;
    }


    public static void putPlot(CKANResource resourceWithMimeType, String plotInfo) {
        plotterMap.put(resourceWithMimeType.id, plotInfo);
    }
}
