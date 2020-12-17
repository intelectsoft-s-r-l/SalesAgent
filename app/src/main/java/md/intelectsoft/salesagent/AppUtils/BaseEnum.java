package md.intelectsoft.salesagent.AppUtils;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Tony on 2017/12/3.
 */

public class BaseEnum {
    public static final int Draft = 0, InQueue = 1, InWork = 2, Prepared = 3, Final = 6;
    public static final int DocumentRedactabil = 543, DocumentOnlyPreview = 2321, DocumentNou = 123;
    public static final int Syncronizat = 0, GataPentruSincronizare = 1, NeSincronizat = 2, InProcesDeSincronizare = 3;

    @IntDef({Draft, InQueue, InWork, Prepared, Final})
    @Retention(RetentionPolicy.SOURCE)
    public @interface stateOrders {
    }

    @IntDef({Syncronizat, GataPentruSincronizare, NeSincronizat, InProcesDeSincronizare})
    @Retention(RetentionPolicy.SOURCE)
    public @interface stateSync {
    }

    @IntDef({DocumentRedactabil, DocumentOnlyPreview, DocumentNou})
    @Retention(RetentionPolicy.SOURCE)
    public @interface stateDoc {
    }
}
