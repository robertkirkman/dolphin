package org.dolphinemu.dolphinemu.fragments

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.dolphinemu.dolphinemu.features.settings.model.BooleanSetting
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.dolphinemu.dolphinemu.databinding.FragmentGridOptionsBinding
import org.dolphinemu.dolphinemu.databinding.FragmentGridOptionsTvBinding
import org.dolphinemu.dolphinemu.features.settings.model.NativeConfig
import org.dolphinemu.dolphinemu.ui.main.MainView

class GridOptionDialogFragment : BottomSheetDialogFragment() {

    private lateinit var mView: MainView

    private var _mBindingMobile: FragmentGridOptionsBinding? = null
    private var _mBindingTv: FragmentGridOptionsTvBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val mBindingMobile get() = _mBindingMobile!!
    private val mBindingTv get() = _mBindingTv!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mView = (activity as MainView)

        if (activity is AppCompatActivity)
        {
            _mBindingMobile = FragmentGridOptionsBinding.inflate(inflater, container, false)
            return mBindingMobile.root
        }
        _mBindingTv = FragmentGridOptionsTvBinding.inflate(inflater, container, false)
        return mBindingTv.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (activity is AppCompatActivity) {
            setUpCoverButtons()
            setUpTitleButtons()

            // Pins fragment to the top of the dialog ensures the dialog is expanded in landscape by default
            BottomSheetBehavior.from<View>(mBindingMobile.gridSheet).state =
                BottomSheetBehavior.STATE_EXPANDED
            dialog?.setOnShowListener {
                val dialog = it as BottomSheetDialog
                mBindingMobile.gridSheet.let { sheet ->
                    dialog.behavior.peekHeight = sheet.height
                }
            }
        } else {
            setUpCoverButtonsTv()

            // Pins fragment to the top of the dialog ensures the dialog is expanded in landscape by default
            BottomSheetBehavior.from<View>(mBindingTv.gridSheet).state =
                BottomSheetBehavior.STATE_EXPANDED
            dialog?.setOnShowListener {
                val dialog = it as BottomSheetDialog
                mBindingTv.gridSheet.let { sheet ->
                    dialog.behavior.peekHeight = sheet.height
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBindingMobile = null
        _mBindingTv = null
    }

    private fun setUpCoverButtons() {
        mBindingMobile.switchDownloadCovers.isChecked =
            BooleanSetting.MAIN_USE_GAME_COVERS.booleanGlobal
        mBindingMobile.rootDownloadCovers.setOnClickListener {
            mBindingMobile.switchDownloadCovers.isChecked = !mBindingMobile.switchDownloadCovers.isChecked
        }
        mBindingMobile.switchDownloadCovers.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
            BooleanSetting.MAIN_USE_GAME_COVERS.setBooleanGlobal(
                NativeConfig.LAYER_BASE,
                mBindingMobile.switchDownloadCovers.isChecked
            )
            mView.reloadGrid()
        }
    }

    private fun setUpTitleButtons() {
        mBindingMobile.switchShowTitles.isChecked = BooleanSetting.MAIN_SHOW_GAME_TITLES.booleanGlobal
        mBindingMobile.rootShowTitles.setOnClickListener {
            mBindingMobile.switchShowTitles.isChecked = !mBindingMobile.switchShowTitles.isChecked
        }
        mBindingMobile.switchShowTitles.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
            BooleanSetting.MAIN_SHOW_GAME_TITLES.setBooleanGlobal(
                NativeConfig.LAYER_BASE,
                mBindingMobile.switchShowTitles.isChecked
            )
            mView.reloadGrid()
        }
    }

    // TODO: Remove this when leanback is removed
    private fun setUpCoverButtonsTv() {
        mBindingTv.switchDownloadCovers.isChecked =
            BooleanSetting.MAIN_USE_GAME_COVERS.booleanGlobal
        mBindingTv.rootDownloadCovers.setOnClickListener {
            mBindingTv.switchDownloadCovers.isChecked = !mBindingTv.switchDownloadCovers.isChecked
        }
        mBindingTv.switchDownloadCovers.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
            BooleanSetting.MAIN_USE_GAME_COVERS.setBooleanGlobal(
                NativeConfig.LAYER_BASE,
                mBindingTv.switchDownloadCovers.isChecked
            )
            mView.reloadGrid()
        }
    }
}
