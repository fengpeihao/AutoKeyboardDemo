package com.cfxc.autokeyboarddemo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.cfxc.autokeyboarddemo.R
import com.cfxc.autokeyboarddemo.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * @Description:
 * @Author: hao
 * @Date: 2021/2/1
 */
class HomeFragment : BaseFragment() {

    override val isShowToolbar = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_not_need_scroll.setOnClickListener { findNavController().navigate(R.id.action_start_not_need_scroll_fragment) }
        btn_need_scroll.setOnClickListener { findNavController().navigate(R.id.action_start_need_scroll_fragment) }
    }

}