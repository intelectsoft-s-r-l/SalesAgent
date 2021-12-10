package md.intelectsoft.salesagent.RealmUtils;
import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by Igor on 20.12.2019
 */

public class RealmMigrations implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();
        if(oldVersion == 1){
            schema.get("Client").addField("image", byte[].class);

            oldVersion++;
        }
        if(oldVersion == 2){
            schema.get("Assortment")
                    .addField("unitInPackage", String.class)
                    .addField("nonWhole", boolean.class)
                    .addField("countInPackage", Double.class);

            oldVersion++;
        }
        if(oldVersion == 3){
            schema.get("Assortment")
                    .addField("image", byte[].class);
            oldVersion++;
        }
        if(oldVersion == 4){
            schema.get("Request")
                    .addField("name", String.class)
                    .addField("surName", String.class)
                    .addField("phone", String.class)
                    .addField("additionalInfo", String.class)
                    .addField("savedDataComment", boolean.class);

            oldVersion++;
        }
        if(oldVersion == 5){
            schema.get("Request")
                    .removeField("savedDataComment");
            schema.get("Client")
                    .addField("savedDataComment", boolean.class);
            oldVersion++;
        }
        if(oldVersion == 6){
            schema.get("Request")
                    .removeField("additionalInfo")
                    .removeField("surName")
                    .removeField("name")
                    .removeField("phone");
            schema.get("Client")
                    .addField("namePerson", String.class)
                    .addField("surName", String.class)
                    .addField("phone", String.class)
                    .addField("address", String.class)
                    .addField("additionalInfo", String.class);
            oldVersion++;
        }
    }
}
