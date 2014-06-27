package de.hsbremen.mds.android.ingame;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import de.hsbremen.mds.common.guiobjects.MdsItem;
import de.hsbremen.mds.mdsandroid.R;
public class CustomGrid extends BaseAdapter{
    private Context mContext;
    private ArrayList<MdsItem> itemList;
    private ImageLoader imageLoader;
    
      public CustomGrid(Context c, ArrayList<MdsItem> itemList) {
          mContext = c;
          this.itemList = itemList;
          this.imageLoader = new ImageLoader();
      }
    @Override
    public int getCount() {
      return itemList.size();
    }
    @Override
    public Object getItem(int position) {
      return null;
    }
    @Override
    public long getItemId(int position) {
      return 0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View grid;
      LayoutInflater inflater = (LayoutInflater) mContext
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
          if (convertView == null) {
            grid = new View(mContext);
        grid = inflater.inflate(R.layout.grid_single, null);
            TextView textView = (TextView) grid.findViewById(R.id.grid_text);
            ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);
            textView.setText(itemList.get(position).getName());
            Bitmap b = imageLoader.getBitmapFromURL(itemList.get(position).getImagePath());
            imageView.setImageBitmap(b);
          } else {
            grid = (View) convertView;
          }
      return grid;
    }
}