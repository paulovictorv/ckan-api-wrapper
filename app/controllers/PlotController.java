package controllers;

import br.wrapper.ckanclient.model.CKANResource;
import br.wrapper.ckanclient.model.DatasetDescription;
import org.jdeferred.DoneCallback;
import play.mvc.Controller;
import play.mvc.Result;
import plugin.CKAN;
import util.ValueHolder;


/**
 * Created by Paulo on 17/06/14.
 */
public class PlotController extends Controller {

    public static Result plotResource(final String dataset) throws InterruptedException {
        final String accepts = ctx().request().getHeader("Accept");
        final ValueHolder<String> streamHolder = new ValueHolder<>();

        CKAN.client
                .getDataset(dataset)
                .then(new DoneCallback<DatasetDescription>() {
                    @Override
                    public void onDone(DatasetDescription datasetDescription) {
                        if (datasetDescription.resources.size() != 0) {
                            streamHolder.setValue(PlotRepository.obtainPlot(datasetDescription.resources.get(0)));
                        } else {
                            throw new NullPointerException();
                        }
                    }
                }
                ).waitSafely();

        response().setContentType("application/json");
        return ok(streamHolder.getValue());
    }

    public static Result putPlot(final String dataset) throws InterruptedException {
        final String accepts = ctx().request().getHeader("Accept");
        final ValueHolder<String> streamHolder = new ValueHolder<>();

        CKAN.client
                .getDataset(dataset)
                .then(new DoneCallback<DatasetDescription>() {
                    @Override
                    public void onDone(DatasetDescription datasetDescription) {
                        CKANResource resourceWithMimeType = datasetDescription.getResourceWithMimeType(accepts);
                        if (resourceWithMimeType != null) {
                            PlotRepository.putPlot(resourceWithMimeType, request().body().asText());
                        } else {
                            throw new NullPointerException();
                        }
                    }
                }
                ).waitSafely();
        return ok();
    }

}
