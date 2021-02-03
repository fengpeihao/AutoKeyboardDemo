package com.fengpeihao.library

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.ScrollView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.widget.NestedScrollView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView


/**
 * @Description: support bottom button auto move up when the keyboard show
 * @Author: hao
 * @Date: 2020/12/28
 */
class AutoKeyboardConstraintLayout : ConstraintLayout {

    // not need move up
    private val NORMAL = 0

    // bottom view move up, the above view move up as need
    private val AUTO_TRANSLATION = 1

    /** bottom view move up, and change the scrollView height
     * Note: When used this type, that need to have a ScrollView or NestedScrollView or RecyclerView in the layout, and set the above view's id is it's id
     **/
    private val AUTO_SCROLL = 2

    // the id of the views that need move up
    private var bottomViewIds = arrayListOf<Int>()

    //the id of the view above of the bottom view
    private var aboveViewId: Int = NO_ID
    private var onGlobalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var rootViewVisibleHeight: Int = 0
    private var rootViewHeight: Int = 0

    // This margin is the height from the keyboard when the bottom view moves up
    private var viewMargin: Float = 10f

    //the type of that need move up
    private var autoType = NORMAL
    private val animatorDuration = 100L
    private var toolbarHeight: Int = 0

    constructor(context: Context) : super(context) {
        initViews(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initViews(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initViews(attrs)
    }

    @SuppressLint("CustomViewStyleable")
    private fun initViews(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoKeyboardConstraintLayout)
            val idStrings = typedArray.getString(R.styleable.AutoKeyboardConstraintLayout_bottomViewIds)
            setBottomViewID(idStrings)
            aboveViewId = typedArray.getResourceId(R.styleable.AutoKeyboardConstraintLayout_aboveViewId, NO_ID)
            viewMargin = typedArray.getDimension(R.styleable.AutoKeyboardConstraintLayout_autoMargin, 10f)
            autoType = typedArray.getInt(R.styleable.AutoKeyboardConstraintLayout_autoType, NORMAL)
            typedArray.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (autoType != NORMAL) {
            onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                //Gets the size of the current root view displayed on the screen
                val r = Rect()
                getWindowVisibleDisplayFrame(r)

                val visibleHeight = r.height()
                rootViewHeight = rootViewHeight.coerceAtLeast(visibleHeight)
                if (rootViewVisibleHeight == 0) {
                    rootViewVisibleHeight = visibleHeight
                    return@OnGlobalLayoutListener
                }
                //The display height of the root view doesn't change
                if (rootViewVisibleHeight == visibleHeight) {
                    return@OnGlobalLayoutListener
                }

                //The display height of the root view has been reduced to over 200 and can be viewed as a soft keyboard display
                if (rootViewHeight - visibleHeight > 200) {
                    val keyboardHeight = rootViewHeight - visibleHeight

                    changeLayout(keyboardHeight)
                    rootViewVisibleHeight = visibleHeight
                    return@OnGlobalLayoutListener
                }

                //The root view shows a larger height than 200, which can be seen as a soft keyboard hidden
                if (visibleHeight - rootViewVisibleHeight > 200) {
                    //keyboard hide
                    restoreView()
                    rootViewVisibleHeight = visibleHeight
                    return@OnGlobalLayoutListener
                }
            }
            viewTreeObserver?.addOnGlobalLayoutListener(onGlobalLayoutListener)
        }
    }

    private fun changeLayout(keyboardHeight: Int) {
        when (autoType) {
            AUTO_TRANSLATION, AUTO_SCROLL -> translationBottomView(keyboardHeight)
        }
    }

    private fun translationBottomView(keyboardHeight: Int) {
        //this is to support toolbar
        val parentView = getRootView(parent)
        // parentView is MainActivity root view
        parentView?.let {drawerLayout->
            // the height of the toolbar is the view in the MainActivity layout
            if (drawerLayout is ViewGroup){
                val toolbarParent = drawerLayout[0]
                if(toolbarParent is ViewGroup && toolbarParent[0] is Toolbar&& toolbarParent[0].isShown){
                    toolbarHeight = toolbarParent[0].height
                }
            }
        }
        var translationHeight = 0f
        var isNeedBottomTranslation = false
        // the lowest view of the bottom views
        var lowestView: View? = null
        // the highest view of the bottom views
        var highestView: View? = null
        var needHeight = 0f
        for (item in bottomViewIds) {
            val bottomView = getViewById(item)
            bottomView?.let {
                if (bottomView.visibility == View.VISIBLE) {
                    lowestView = if (bottomView.bottom > lowestView?.bottom ?: 0) bottomView else lowestView
                    highestView = if (bottomView.top < highestView?.top ?: rootViewHeight) bottomView else highestView
                }
            }
        }

        val aboveView = if (aboveViewId == NO_ID) null else getViewById(aboveViewId)
        var aboveViewBottom = 0
        aboveView?.apply {
            aboveViewBottom = bottom
        }

        lowestView?.let { lowest ->
            if (this.height - lowest.bottom >= keyboardHeight) return
            highestView?.let { highest ->
                val bottomViewsHeight = lowest.bottom - highest.top
                if (aboveViewBottom == 0
                        || keyboardHeight + bottomViewsHeight + dip2px(viewMargin * 2) < this.height - aboveViewBottom
                        || autoType == AUTO_SCROLL) {
                    needHeight = keyboardHeight - (this.height - lowest.bottom) + dip2px(viewMargin) - getNavigationBarHeight()
                    translationHeight = 0f
                    isNeedBottomTranslation = true
                } else {
                    needHeight = keyboardHeight + aboveViewBottom + bottomViewsHeight + dip2px(viewMargin * 2) - getNavigationBarHeight()
                    translationHeight = this.height - needHeight
                    isNeedBottomTranslation = lowest.bottom + translationHeight > this.height - keyboardHeight - dip2px(viewMargin)
                }
            }
        }

        if (isNeedBottomTranslation)
            for (item in bottomViewIds) {
                val bottomView = getViewById(item)
                if (bottomView.visibility == View.VISIBLE)
                    bottomView.animate().setDuration(animatorDuration).translationY(this.height - (lowestView?.bottom
                            ?: 0) - keyboardHeight - dip2px(viewMargin) - translationHeight + getNavigationBarHeight()).start()
            }
        if (autoType == AUTO_TRANSLATION && aboveViewBottom > 0 && needHeight > this.height) {
            this.animate().setDuration(animatorDuration).translationY(translationHeight).start()
        }
        if (autoType == AUTO_SCROLL) {
            aboveView?.let {
                if (aboveView is ScrollView || aboveView is NestedScrollView || aboveView is RecyclerView) {
                    val layoutParams = aboveView.layoutParams
                    (layoutParams as LayoutParams).bottomMargin = (needHeight + dip2px(viewMargin)).toInt()
                    aboveView.layoutParams = layoutParams
                }
            }
        }
    }

    private fun getRootView(viewParent: ViewParent?): ViewParent? {
        if (viewParent != null) {
            if (viewParent is DrawerLayout) {
                return viewParent
            } else {
               return getRootView(viewParent.parent)
            }
        } else {
            return null
        }
    }

    private fun restoreView() {
        when (autoType) {
            AUTO_TRANSLATION, AUTO_SCROLL -> restoreTranslation()
        }
    }

    private fun restoreTranslation() {
        for (item in bottomViewIds) {
            val bottomView = getViewById(item)
            bottomView?.let {
                this.animate().setDuration(animatorDuration).translationY(0f)
                it.animate().setDuration(animatorDuration).translationY(0f)
            }
        }
        if (autoType == AUTO_SCROLL) {
            val aboveView = if (aboveViewId == NO_ID) null else getViewById(aboveViewId)
            aboveView?.let {
                if (aboveView is ScrollView || aboveView is NestedScrollView || aboveView is RecyclerView) {
                    val layoutParams = aboveView.layoutParams
                    (layoutParams as LayoutParams).bottomMargin = 0
                    aboveView.layoutParams = layoutParams
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        toolbarHeight = 0
        if (autoType != NORMAL)
            viewTreeObserver?.removeOnGlobalLayoutListener(onGlobalLayoutListener)
    }

    private fun setBottomViewID(idStrings: String?) {
        idStrings?.let {
            val idLists = it.split(",")
            for (item in idLists) {
                addID(item)
            }
        }
    }

    private fun addID(idString: String) {
        val idStr = idString.trim()
        var tag = 0
        try {
            val res: Class<*> = id::class.java
            val field = res.getField(idStr)
            tag = field.getInt(null as Any?)
        } catch (var5: Exception) {
        }
        if (tag == 0) {
            tag = context.resources.getIdentifier(idStr, "id", context.packageName)
        }
        if (tag == 0) {
            val constraintLayout = this.parent as ConstraintLayout
            val value = constraintLayout.getDesignInformation(0, idStr)
            if (value != null && value is Int) {
                tag = value
            }
        }
        if (tag != 0) {
            bottomViewIds.add(tag)
        } else {
            Log.w("AutoKeyboardLayout", "Could not find id of \"$idStr\"")
        }
    }

    private fun dip2px(dpValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dpValue * scale + 0.5f
    }

    // This is to support the navigation bar show hide
    private fun getNavigationBarHeight(): Int {
        return rootViewHeight - this.height - toolbarHeight
    }
}