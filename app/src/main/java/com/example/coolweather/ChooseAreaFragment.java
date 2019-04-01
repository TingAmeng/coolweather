package com.example.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utillity;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;

    private List<String> dataList = new ArrayList<>();

    /**
     * 省列表
     * **/
    private List<Province> provinceList;

    /**
     * 市列表
     * */
    private List<City> cityList;

    /**
     * 县列表
     * */
    private List<County> countyList;

    /**
     * 选中的省份
     * */
    private Province selectedProvince;

    /**
     * 选中的城市
     * */
    private City selectedCity;

    /**
     * 当前选中的级别
     * */
    private int currentLevel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //动态加载 choose_area.xml
        View view = inflater.inflate(R.layout.choose_area,container,false);
        //获取控件实例
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        //初始化 ArrayAdapter ,并设置为 ListView的适配器
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.
                simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    //给 ListView 和 Button 设置点击事件
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        queryProvinces();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if(currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    /*instanceof 关键字判断一个对象 是否属于某个类的实例
                    * 再 Fragment 中调用 getActivity() ,让后配合 instanceof 关键字
                    * 判断该 Fragment 是否再 MainActivity 中
                    * */
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        //跳转至 WeatherActivity
                        startActivity(intent);
                        getActivity().finish();
                    } else if(getActivity() instanceof WeatherActivity){
                        //获得 WeatherActivity 实例
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        //关闭 滑动菜单
                        activity.drawerLayout.closeDrawers();
                        //显示 刷新进度条
                        activity.swipeRefresh.setRefreshing(true);
                        //获取天气信息，并显示到主界面
                        activity.requestWeather(weatherId);
                    }
                }
            }
        });
        //根据当前 currentLevel 处理对应的返回逻辑
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel == LEVEL_COUNTY){
                    queryCities();
                } else if(currentLevel ==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
    }

    /**
     * 查询全国所有的省，优先从数据库查询，如果没有查询到，再去服务器上去查询
     * 如果查询到了数据就直接显示到界面上
     * */
    private void queryProvinces(){
        titleText.setText("中国");
        //隐藏 标题栏的返回键
        backButton.setVisibility(View.GONE);
        //调用 LitaPal的查询接口从数据库查询数据
        provinceList = LitePal.findAll(Province.class);
        if(provinceList.size() > 0){
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            //通知listView适配器更新数据
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            //当前 列表深度
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     * */
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceid = ?",String.valueOf
                (selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0){
            dataList.clear();
            for(City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProviceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     * */
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?",String.valueOf
                (selectedCity.getId())).find(County.class);
        if(countyList.size() > 0){
            dataList.clear();
            for(County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectedProvince.getProviceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/"
                    + provinceCode + "/"
                    + cityCode;
            queryFromServer(address,"county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     * */
    private void queryFromServer(String address,final String type){
        //显示进度对话框
        showProgressDialog();

        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            //Http请求失败
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载数据失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            //服务器响应的数据会回调到 onResponse()方法中
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                /*调用Utillity解析从服务器获取的数据，此方法封装了 解析Json数据
                    并储存到数据库对应的表中*/
                if("province".equals(type)){
                    result = Utillity.handleProvinceResponse(responseText);
                } else if("city".equals(type)){
                    result = Utillity.handleCityResponse(responseText,selectedProvince.getId());
                } else if("county".equals(type)){
                    result = Utillity.handleCountyResponse(responseText,selectedCity.getId());
                }
                /*解析完所有json数据，并写入数据库对应表中。完成之后*/
                if(result){
                    /*通过 runOnUiThread() 回到主线程 处理逻辑，
                    * 再次加载,由于queryProvinces()方法牵扯到 UI 操作，必须要回到主线程调用*/
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //关闭进度条对话框
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            } else if ("city".equals(type)){
                                queryCities();
                            } else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     * */
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度条对话框
     * */
    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}
