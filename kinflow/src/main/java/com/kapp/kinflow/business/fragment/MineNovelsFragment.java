package com.kapp.kinflow.business.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kapp.kinflow.R;
import com.kapp.kinflow.R2;
import com.kapp.kinflow.business.adapter.MineNovelsItemFactory;
import com.kapp.kinflow.business.beans.MineNovelsBean;
import com.kapp.kinflow.view.recyclerview.IItemFactory;
import com.kapp.kinflow.view.recyclerview.adapter.RecycleViewCommonAdapter;
import com.kapp.knews.base.recycler.decoration.ItemDecorationDivider;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * description：我的小说页面
 * <br>author：caowugao
 * <br>time： 2017/05/19 17:33
 */

public class MineNovelsFragment extends Fragment {

    @BindView(R2.id.tv_total)
    TextView tvTotal;
    @BindView(R2.id.tv_operation)
    TextView tvOperation;
    @BindView(R2.id.recyclerview)
    RecyclerView recyclerview;
    private List<MineNovelsBean> datas;
    private RecycleViewCommonAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_mine_novels, null, false);
        ButterKnife.bind(this, contentView);
        initViews(contentView);
        initData();
        return contentView;
    }

    private void initViews(View contentView) {
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));
        recyclerview.addItemDecoration(new ItemDecorationDivider(getContext(), LinearLayoutManager.VERTICAL));
    }

    @OnClick(R2.id.tv_operation)
    public void onOperationClick(View view) {
        if (null != datas) {
            for (MineNovelsBean bean : datas) {
                bean.isEdite = !bean.isEdite;
            }
            adapter.notifyItemRangeChanged(0, datas.size());
        }

    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//
//        if (getUserVisibleHint()) {
//            onVisible();
//        } else {
//            onInvisible();
//        }
//    }
//
//    private void onInvisible() {
//
//    }
//
//    private void onVisible() {
//        initData();
//    }

    private void initData() {
        int size = 8;
        datas = new ArrayList<>(size);
        String imagrUrl = "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=210994475,2821007383&fm=23&gp=0" +
                ".jpg";
        for (int i = 0; i < size; i++) {
            MineNovelsBean bean = new MineNovelsBean(imagrUrl, "title" + i, true, i, false, "clickUrl" + i);
            datas.add(bean);
        }
        IItemFactory factory = new MineNovelsItemFactory();
        adapter = new RecycleViewCommonAdapter(datas, recyclerview, factory);
        recyclerview.setAdapter(adapter);
    }
}
