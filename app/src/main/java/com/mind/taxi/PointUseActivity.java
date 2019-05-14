package com.mind.taxi;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Random;


public class PointUseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_use);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");

        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.btn_ham_dark_selector));

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        ArrayList<Product> products_list = new ArrayList<>();
        Random random = new Random();

//        for (int i = 0; i < 20; i++) {
//            products_list.add(new Product(i + "번 상품", random.nextInt(500) + " 점", R.mipmap.ic_launcher));
//        }

        products_list.add(new Product("자동차 부동액", "10,000 점", R.drawable.img1));
        products_list.add(new Product("엔진 관리용품", "20,000 점", R.drawable.img2));
        products_list.add(new Product("자동차 방향제", "17,000 점", R.drawable.img3));
        products_list.add(new Product("블루투스 스피커", "30,000 점", R.drawable.img4));

        GridAdapter grid_adapter = new GridAdapter(PointUseActivity.this, products_list);

        GridView grid = (GridView) findViewById(R.id.point_items_list);
        grid.setAdapter(grid_adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                selected_data = MapsActivity.selected_data_list.get(i);
//                startActivity(new Intent(GalleryActivity.this, GalleryViewActivity.class));
            }
        });
        grid.setSelector(new StateListDrawable());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            grid.setNestedScrollingEnabled(true);
        }


    }


    class GridAdapter extends BaseAdapter {
        Context con;
        ArrayList<Product> data_list;
        LayoutInflater inflater;

        GridAdapter(Context con, ArrayList<Product> data_list) {
            this.data_list = data_list;
            this.con = con;
            inflater = (LayoutInflater) this.con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }

        @Override
        public int getCount() {
            return data_list.size();
        }

        @Override
        public Object getItem(int i) {
            return data_list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convert_view, ViewGroup viewGroup) {
            convert_view = inflater.inflate(R.layout.point_sale_item, viewGroup, false);
            ImageView img = (ImageView) convert_view.findViewById(R.id.item_img);
            Glide.with(con).load(data_list.get(i).img)
                    .centerCrop()
                    .into(img);
            TextView name = (TextView) convert_view.findViewById(R.id.name);
            TextView point = (TextView) convert_view.findViewById(R.id.point);
            name.setText(data_list.get(i).name);
            point.setText(data_list.get(i).point);
            return convert_view;
        }
    }
}
