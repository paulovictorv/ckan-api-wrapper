package plugin;

import br.wrapper.ckanclient.CKANClient;
import play.Application;
import play.Logger;
import play.Plugin;

import java.net.URISyntaxException;

/**
 * Created by Paulo on 08/06/14.
 */
public class CKAN extends Plugin {

    public static CKANClient client;

    private Application app;

    public CKAN(Application application){
        app = application;
    }

    public CKAN() {
        super();
    }

    @Override
    public void onStart() {
        String ckanURL = app.configuration().getString("ckan.url");

        try {
            client = new CKANClient(ckanURL);
            Logger.info("CKANClient initialized");
        } catch (URISyntaxException e) {
            Logger.error("Error while initiliazing CKAN client: ", e);
        }

        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean enabled() {
       return true;
    }


    public void $init$() {
    }
}
