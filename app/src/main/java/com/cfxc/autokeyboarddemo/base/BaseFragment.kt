package com.cfxc.autokeyboarddemo.base

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.cfxc.autokeyboarddemo.MainActivity

/**
 * @Description:
 * @Author: peihao.feng
 * @Date: 2021/2/1
 */
open class BaseFragment : Fragment() {

   protected open val isShowToolbar: Boolean = true


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val supportActionBar = (requireActivity() as MainActivity).supportActionBar
        if (isShowToolbar) {
            supportActionBar?.show()
        } else {
            supportActionBar?.hide()
        }
    }
}