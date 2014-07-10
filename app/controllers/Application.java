package controllers;

import br.wrapper.ckanclient.model.CKANResource;
import br.wrapper.ckanclient.model.DatasetDescription;
import org.jdeferred.DoneCallback;
import org.jdeferred.DonePipe;
import org.jdeferred.Promise;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import plugin.CKAN;
import util.ValueHolder;

import java.io.InputStream;
import java.util.List;

public class Application extends Controller {
    /**
     * Maps a Dataset to the resource specified by the header. If there was no Accept header on the request, it returns
     * the first available representation
     *
     * @param dataset
     * @return HTTP Result
     */
   public static F.Promise<Result> resourceForDataset(final String dataset){
       final String accepts = ctx().request().getHeader("Accept");
       final ValueHolder<InputStream> streamHolder = new ValueHolder<>();
       F.Promise<Result> promise = F.Promise.promise(new F.Function0<Result>() {
           @Override
           public Result apply() throws Throwable {
                    CKAN.client
                       .getDataset(dataset)
                       .then(new DonePipe<DatasetDescription, InputStream, Throwable, Double>() {
                           @Override
                           public Promise<InputStream, Throwable, Double> pipeDone(DatasetDescription datasetDescription) {
                               CKANResource resourceWithMimeType = datasetDescription.getResourceWithMimeType(accepts);
                               if(resourceWithMimeType == null){
                                   //Means that the resource does not have the requested representation
                                   //Try to find another (xml, csv, xls) and convert to json

                                   List<CKANResource> resources = datasetDescription.resources;
                                   if (resources.size() == 0){
                                       throw new NullPointerException();
                                   } else {
                                       Promise<InputStream, Throwable, Double> stream = null;
                                       for (CKANResource resource : resources) {
                                           switch(resource.mimeType){
                                               case "application/xml":
                                                   stream = JsonConverter.xml2json(resource.getResource());
                                                   break;
                                               case "application/csv":
                                                   stream = JsonConverter.csv2json(resource.getResource());
                                                   break;
                                               case "application/vnd.ms-excel":
                                                   stream = JsonConverter.xls2json(resource.getResource());
                                                   break;
                                               default:
                                                   return null;
                                           }
                                       }
                                       return stream;
                                   }
                               } else {
                                   return resourceWithMimeType.getResource();
                               }
                           }
                       })
                    .then(new DoneCallback<InputStream>() {
                        @Override
                        public void onDone(InputStream oStream) {
                            streamHolder.setValue(oStream);
                        }
                    }
                    ).waitSafely();
               InputStream value = streamHolder.getValue();
               if (value == null){
                   return badRequest();
               } else {
                   response().setContentType(accepts);
                   return ok(value);
               }
           }
       });

       return promise;
    }

    /**
     * List all the datasets available
     *
     * @return
     */
   public static Result listResources(){
       response().setContentType("application/json");
       return ok("[\"ideb-total-8-serie-do-ensino-fundamental\"," +
                 "\"indicadores-taxa-de-reprovaaao-ensino-madio-rede-privada\"," +
                 "\"moradores-em-domicilios-particulares-permanentes-urbana\"]");
   }
}
