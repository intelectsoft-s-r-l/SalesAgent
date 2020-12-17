package md.intelectsoft.salesagent.OrderServiceUtils;

/**
 * Created by Igor on 07.09.2020
 */

public class RemoteException {
    public static String getServiceException(int errorCode) {

        switch (errorCode){
            case -1:
                return "Eroare internă";
            case 0:
                return "None!";
            case 1:
                return "Parolă sau login incorect!";
            case 2:
                return "Utilizator nu a fost găsit!";
            case 3:
                return "Utilizatorul nu este activ!";
            case 4:
                return "Excepție de blocare!";
            case 5:
                return "Token nevalid!";
            case 6:
                return "Token expirat!";
            case 101:
                return "Depozitul nu a fost găsit!";
            case 102:
                return "Oficiul nu a fost gasit!";
            case 103:
                return "Documentul nu a fost găsit!";
            case 201:
                return "Se procesează documentul restricționat!";
            default:
                return "Eroare necunoscută";
        }
    }
}
