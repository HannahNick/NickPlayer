package com.xyz.edu.util;

import android.view.View;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 2019-04-29.
 */
public class ListAdapterUtil {

    public static final int PAGE_SIZE = 20;

    private ListAdapterUtil(){}


    /**
     * 完成下拉刷新和上拉加载逻辑
     * @param refreshLayout 刷新控件
     * @param adapter 数据适配器
     * @param data 数据
     * @param emptyView 空数据页面
     * @param <T> 数据类型
     */
    public static<T> void setUpData(SwipeRefreshLayout refreshLayout,
                                    BaseQuickAdapter<T, BaseViewHolder> adapter,
                                    List<T> data,
                                    View emptyView){
        if (refreshLayout.isRefreshing()){
            refreshLayout.setRefreshing(false);
            setUpData(true,adapter,data,emptyView);
        }else {
            setUpData(false,adapter,data,emptyView);
        }
    }


    /**
     * 刷新或加载数据,并添加空布局
     * @param isRefresh 是否刷新
     * @param adapter 数据适配器
     * @param data 数据
     * @param emptyView 空数据页面
     * @param <T> 数据类型
     */
    public static<T> void setUpData(boolean isRefresh,
                                    BaseQuickAdapter<T, BaseViewHolder> adapter,
                                    List<T> data,View emptyView){
        if (data==null){
            data = new ArrayList<>();
        }
        if (isRefresh){
            adapter.replaceData(data);
            adapter.loadMoreComplete();
            if (emptyView!=null){
                adapter.setEmptyView(emptyView);
            }
        }else {
            adapter.addData(data);
            if (data.size()>=PAGE_SIZE){
                adapter.loadMoreComplete();
            }
        }
        if (data.size()<PAGE_SIZE){
            adapter.loadMoreEnd();
        }
    }

    /**
     * 刷新数据
     * @param adapter 数据适配器
     * @param data 数据
     * @param emptyView 空数据页面
     * @param <T> 数据类型
     */
    public static<T> void setUpData(BaseQuickAdapter<T, BaseViewHolder> adapter,
                                    List<T> data,View emptyView){
        setUpData(true,adapter,data,emptyView);
    }

}
