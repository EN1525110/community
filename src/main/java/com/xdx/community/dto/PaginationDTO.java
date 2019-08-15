package com.xdx.community.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PaginationDTO<T> {
    private List<T> data; //要展示的数据集合
    private boolean showPrevious;  //是否展示上一页
    private boolean showFirstPage; //是否展示首页
    private boolean showNext;     //是否展示下一页
    private boolean showEndPage;  //是否展示尾页面
    private Integer page;        //当前页面
    private Integer totalPage;  //总页数
    private List<Integer> pages = new ArrayList<>();

    public void setPagination(Integer totalPage, Integer page) {
        this.totalPage = totalPage;
        this.page = page;

        pages.add(page);
        for (int i = 1; i <= 3; i++) {
            if (page - i > 0) {
                pages.add(0, page - i);
            }

            if (page + i <= totalPage) {
                pages.add(page + i);
            }
        }

        // 是否展示上一页
        if (page == 1) {
            showPrevious = false;//当前页等于第一页时候，就不展示跳转上一页的按钮
        } else {
            showPrevious = true;
        }

        // 是否展示下一页
        if (page == totalPage) {  //当前页等于最后一页时候，就不展示跳转下一页的按钮
            showNext = false;
        } else {
            showNext = true;
        }

        // 是否展示第一页
        if (pages.contains(1)) { //当显示的页数集合中包含第一页时候，就不显示跳往首页的按钮
            showFirstPage = false;
        } else {
            showFirstPage = true;
        }

        // 是否展示最后一页
        if (pages.contains(totalPage)) {//当显示的页数集合中包含最后一页时候，就不显示跳往末页的按钮
            showEndPage = false;
        } else {
            showEndPage = true;
        }
    }
}
