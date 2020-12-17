package md.intelectsoft.salesagent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import io.realm.RealmList;
import md.intelectsoft.salesagent.Adapters.AdapterLinesItemKind;
import md.intelectsoft.salesagent.RealmUtils.RequestLine;

public class ItemKindDetailFragment extends Fragment {
    RealmList<RequestLine> lines;

    public ItemKindDetailFragment(RealmList<RequestLine> lines) {
        this.lines = lines;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.itemkind_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (rootView != null) {

            ((ListView) rootView.findViewById(R.id.itemkind_list)).setAdapter(new AdapterLinesItemKind(lines));
        }

        return rootView;
    }
}