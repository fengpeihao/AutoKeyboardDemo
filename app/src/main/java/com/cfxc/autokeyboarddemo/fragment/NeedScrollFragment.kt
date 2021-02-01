package com.cfxc.autokeyboarddemo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cfxc.autokeyboarddemo.R
import com.cfxc.autokeyboarddemo.base.BaseFragment

/**
 * @Description:
 * @Author: hao
 * @Date: 2021/2/1
 */
class NeedScrollFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_need_scroll, container, false)
    }
}