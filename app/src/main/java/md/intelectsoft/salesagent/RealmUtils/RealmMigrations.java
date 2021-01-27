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
    }
}
