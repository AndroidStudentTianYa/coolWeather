package com.hfw.coolweather;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hfw.coolweather.db.City;
import com.hfw.coolweather.db.County;
import com.hfw.coolweather.db.Province;
import com.hfw.coolweather.utils.HttpUtil;
import com.hfw.coolweather.utils.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    private Button bt_back;
    private TextView tv_title;
    private ListView lv;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private final int LEVEL_PROVINCE = 1;
    private final int LEVEL_CITY = 2;
    private final int LEVEL_COUNTY = 3;
    private int current_level = 0;
    private ProgressDialog dialog;
    private Province selectedProvince;
    private City selectedCity;
    private County seletedCounty;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_choose_area,container,false);
        bt_back = (Button) v.findViewById(R.id.bt_back);
        tv_title = (TextView) v.findViewById(R.id.tv_title);
        lv = (ListView) v.findViewById(R.id.lv);
        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,dataList);
        lv.setAdapter(adapter);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        initEvent();
        showQueryDialog();
        queryProvince();
        super.onActivityCreated(savedInstanceState);
    }

    private void showQueryDialog() {
        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("正在玩命加载中....");
        dialog.setTitle("提示");
        dialog.show();
    }
    private void dismissDialog()
    {
        if(dialog!=null)
        {
            dialog.dismiss();
        }
    }
    private void queryProvince() {
        tv_title.setText("中国");
        bt_back.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList ==null|| provinceList.size()==0)
        {
            //没找到
            queryProvinceFromServer();
        }
        else {
            dataList.clear();
            for(Province province:provinceList)
            {
                dataList.add(province.getProvinceName());
            }
            current_level = LEVEL_PROVINCE;
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            dismissDialog();
        }
    }

    private void queryServer(String url, final String type) {
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissDialog();
                        Toast.makeText(getActivity(), "抱歉,网络通讯失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                boolean result = false;
                switch(type)
                {
                    case "province":
                        result = Utility.handleProvinceResponse(response.body().string());//查询服务器保存数据库
                        break;
                    case "city":
                        result = Utility.handleCityResponse(response.body().string(),selectedProvince.getId());
                        break;
                    case "county":
                        result = Utility.handleCountyResponse(response.body().string(),selectedCity.getId());
                        break;
                }
                if(result)
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (type)
                            {
                                case "province":
                                    queryProvince();
                                    break;
                                case "city":
                                    queryCity();
                                    break;
                                case "county":
                                    queryCounty();
                                    break;
                            }
                        }
                    });
                }
            }
        });
    }

    private void initEvent() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (current_level)
                {
                    case LEVEL_PROVINCE:
                        selectedProvince = provinceList.get(i);
                        queryCity();
                        break;
                    case LEVEL_CITY:
                        selectedCity = cityList.get(i);
                        queryCounty();
                        break;
                    case LEVEL_COUNTY:
                        seletedCounty = countyList.get(i);
                        queryWeather();
                        break;
                }
            }
        });
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (current_level)
                {
                    case LEVEL_CITY:
                        dataList.clear();
                        for(Province province:provinceList)
                        {
                            dataList.add(province.getProvinceName());
                        }
                        current_level = LEVEL_PROVINCE;
                        adapter.notifyDataSetChanged();
                        tv_title.setText("中国");
                        bt_back.setVisibility(View.GONE);
                        break;
                    case LEVEL_COUNTY:
                        dataList.clear();
                        for(City city:cityList)
                        {
                            dataList.add(city.getCityName());
                        }
                        current_level = LEVEL_CITY;
                        adapter.notifyDataSetChanged();
                        tv_title.setText(selectedProvince.getProvinceName());
                        break;
                }
            }
        });
    }

    private void queryWeather() {
        Toast.makeText(getActivity(), seletedCounty.getCountyName(), Toast.LENGTH_SHORT).show();
    }

    private void queryCounty() {
        tv_title.setText(selectedCity.getCityName());
        bt_back.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId=?",selectedCity.getId()+"").find(County.class);
        if(countyList ==null|| countyList.size()==0)
        {
            //没找到
            queryCountyFromServer();
        }
        else {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            current_level = LEVEL_COUNTY;
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            dismissDialog();
        }
    }

    private void queryCity() {
        tv_title.setText(selectedProvince.getProvinceName());
        bt_back.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceId=?",selectedProvince.getId()+"").find(City.class);
        if(cityList ==null|| cityList.size()==0)
        {
            //没找到
            queryCityFromServer();
        }
        else {
            dataList.clear();
            for(City city:cityList)
            {
                dataList.add(city.getCityName());
            }
            current_level = LEVEL_CITY;
            adapter.notifyDataSetChanged();
            lv.setSelection(0);
            dismissDialog();
    }
}

    private void queryCityFromServer() {
        String url = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode();
        queryServer(url,"city");
    }
    private void queryCountyFromServer() {
        String url = "http://guolin.tech/api/china/"+selectedProvince.getProvinceCode()+"/"+selectedCity.getCityCode();
        queryServer(url,"county");
    }
    private void queryProvinceFromServer() {
        String url = "http://guolin.tech/api/china";
        queryServer(url,"province");
    }
}
