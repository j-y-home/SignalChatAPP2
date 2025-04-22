//package com.example.endtoendencryptionsystem.ui
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.activityViewModels
//import androidx.recyclerview.widget.LinearLayoutManager
//import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider
//import autodispose2.androidx.lifecycle.autoDispose
//import autodispose2.autoDispose
//import com.chad.library.adapter.base.animation.ScaleInAnimation
//import com.example.endtoendencryptionsystem.databinding.FragmentConversationBinding
//import com.google.android.material.checkbox.MaterialCheckBox
//import com.google.android.material.snackbar.Snackbar
//import com.ruins.library.sweet.SweetAlertDialog
//import com.ruins.library.sweet.SweetAlertType
//import com.therouter.TheRouter
//import com.wumingtech.at.ATApplication
//import com.wumingtech.at.R
//import com.wumingtech.at.adapter.TaskRecordAdapter
//import com.wumingtech.at.callback.TaskListDiffCallback
//import com.wumingtech.at.databinding.FragmentTaskRecordBinding
//import com.wumingtech.at.entity.MetadataInfo
//import com.wumingtech.at.entity.SearchParam
//import com.wumingtech.at.entity.TaskRecordVO
//import com.wumingtech.at.entity.dto.CirculationDTO
//import com.wumingtech.at.entity.dto.TemplateDialogDTO
//import com.wumingtech.at.limit.FlowStatus
//import com.wumingtech.at.utils.toPDFViewer
//import com.wumingtech.at.viewmodel.MetadataViewModel
//import com.wumingtech.at.viewmodel.RecordViewModel
//import com.wumingtech.base.utils.ui.isOnline
//
//class ChatMsgFragment : Fragment() {
//    private lateinit var binding: FragmentConversationBinding
//    private lateinit var adapter: TaskRecordAdapter
//    private var ids = ArrayList<Int>()
//    private var selectData = ArrayList<TaskRecordVO>()
//    private val viewModel by activityViewModels<MetadataViewModel>()
//    private val deviceKindMap = mapOf("请选择" to "", "压力容器" to "2000", "压力管道" to "8000")
//    private val READ_EXTERNAL_STORAGE: Int = 0
//
//    @FlowStatus
//    private var type: Int = FlowStatus.TASK_TODO
//    private lateinit var engineeringNo: String
//    private lateinit var unitName: String
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        binding = FragmentTaskRecordBinding.inflate(layoutInflater)
//        arguments?.run {
//            type = this.getInt("type")
//            engineeringNo = this.getString("engineeringNo", "")
//            unitName = this.getString("unitName", "")
//        }
//        binding.toolbar.toolbar.title = "(${engineeringNo})${unitName}"
//        binding.toolbar.toolbar.setNavigationIcon(R.drawable.ic_back)
//        binding.toolbar.toolbar.setNavigationOnClickListener { requireActivity().finish() }
//        binding.spinnerDeviceKind.setItems(deviceKindMap.keys.toList())
//        binding.spinnerDeviceKind.selectItemByIndex(0)
//        adapter = TaskRecordAdapter()
//        adapter.setDiffCallback(TaskListDiffCallback())
//        //设置动画
//        adapter.animationEnable = true
//        adapter.adapterAnimation = ScaleInAnimation()
//        adapter.isAnimationFirstOnly = false
//        when (type) {
//            FlowStatus.TASK_TODO -> {
//                binding.rlSelectAll.visibility = View.GONE
//                binding.rlSelectAllTask.visibility = View.VISIBLE
//                binding.tvLook.visibility = View.VISIBLE
//                binding.tvRecordNumber.visibility = View.GONE
//            }
//
//            FlowStatus.CREATE_RECORD -> {
//                binding.rlSelectAll.visibility = View.VISIBLE
//                binding.rlSelectAllTask.visibility = View.GONE
//                binding.tvLook.visibility = View.GONE
//                binding.tvRecordNumber.visibility = View.VISIBLE
//            }
//        }
//        adapter.addChildClickViewIds(
//            R.id.btn_generate, R.id.ln_select_record, R.id.btn_delete, R.id.btn_add_child_original, R.id.btn_eye,
//            R.id.ln_select_task, R.id.btn_preview, R.id.btn_cancel_circulation
//        )
//        binding.rvList.layoutManager = LinearLayoutManager(activity)
//        binding.rvList.adapter = adapter
//        initListener()
//        getData()
//        return binding.root
//    }
//
//    private fun initListener() {
//        adapter.setOnItemChildClickListener { _, view, position ->
//            when (view.id) {
//                //生成原始记录
//                R.id.btn_generate -> {
//                    generateOriginal(position)
//                }
//                //选择任务
//                R.id.ln_select_task -> {
//                    adapter.data[position].isSelected = !adapter.data[position].isSelected
//                    if (adapter.data[position].isSelected) {
//                        ids.add(adapter.data[position].id)
//                        selectData.add(adapter.data[position])
//                    } else {
//                        ids.remove(adapter.data[position].id)
//                        selectData.remove(adapter.data[position])
//                    }
//                    binding.cbSelectAllTask.isChecked = ids.size == adapter.data.size
//                    adapter.notifyItemChanged(position, adapter.data[position])
//                }
//                //多选原始记录
//                R.id.ln_select_record -> {
//                    adapter.data[position].isSelected = !adapter.data[position].isSelected
//                    if (adapter.data[position].isSelected) {
//                        ids.add(adapter.data[position].id)
//                        selectData.add(adapter.data[position])
//                    } else {
//                        ids.remove(adapter.data[position].id)
//                        selectData.remove(adapter.data[position])
//                    }
//                    binding.cbSelectAll.isChecked = ids.size == adapter.data.size
//                    adapter.notifyItemChanged(position, adapter.data[position])
//                }
//                //删除原始记录
//                R.id.btn_delete -> {
//                    deleteRecord(position)
//                }
//                //生成子项
//                R.id.btn_add_child_original -> {
//                    addChildItemRecord(position)
//                }
//                //查看已生成的原始记录
//                R.id.btn_eye -> {
//                    seeChildItemRecord(position)
//                }
//                //预览所有原始记录
//                R.id.btn_preview -> {
//                    checkPermission(position)
//                }
//                // 取消流转
//                R.id.btn_cancel_circulation -> {
//                    SweetAlertDialog(requireContext(), SweetAlertType.WARNING_TYPE)
//                        .setContentText("您确定要取消流转吗？")
//                        .setConfirmButton("确定") { sweet ->
//                            viewModel.cancelCirculation(setOf(adapter.data[position].id))
//                                .autoDispose(this)
//                                .subscribe({
//                                    sweet.dismissWithAnimation()
//                                }, {})
//                        }
//                        .setCancelButton("取消") { it.dismissWithAnimation() }
//                        .show()
//                }
//            }
//        }
//        binding.spinnerDeviceKind.setOnSpinnerDismissListener {
//            binding.spinnerDeviceKind.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn)
//        }
//        binding.btnSearchDevice.setOnClickListener {
//            val deviceKind = deviceKindMap.keys.toList()[binding.spinnerDeviceKind.selectedIndex]
//            val danweineibubianhao = binding.etInternalCode.text.toString()
//            val equipmentName = binding.etEquipmentName.text.toString()
//            val planNumber = binding.etPlanNumber.text.toString()
//            when (type) {
//                FlowStatus.TASK_TODO -> {
//                    viewModel.getToDoTask(
//                        SearchParam(
//                            engineeringNo,
//                            deviceKindMap[deviceKind].toString(),
//                            danweineibubianhao,
//                            equipmentName,
//                            planNumber
//                        )
//                    )
//                }
//
//                FlowStatus.CREATE_RECORD -> {
//                    viewModel.getEditRecord(
//                        SearchParam(
//                            engineeringNo,
//                            deviceKindMap[deviceKind].toString(),
//                            danweineibubianhao,
//                            equipmentName,
//                            planNumber
//                        )
//                    )
//                }
//            }
//        }
//        // 任务全选
//        binding.cbSelectAllTask.setOnClickListener {
//            ids.clear()
//            selectData.clear()
//            val newData = ArrayList<TaskRecordVO>()
//            adapter.data.forEach { bean ->
//                val newBean = bean.copy()
//                newBean.isSelected = (it as MaterialCheckBox).isChecked
//                newData.add(newBean)
//                if (newBean.isSelected) {
//                    ids.add(bean.id)
//                    selectData.add(bean)
//                }
//            }
//            adapter.setDiffNewData(newData)
//        }
//        //原始记录全选
//        binding.cbSelectAll.setOnClickListener {
//            ids.clear()
//            selectData.clear()
//            val newData = ArrayList<TaskRecordVO>()
//            adapter.data.forEach { bean ->
//                val newBean = bean.copy()
//                newBean.isSelected = (it as MaterialCheckBox).isChecked
//                newData.add(newBean)
//                if (newBean.isSelected) {
//                    ids.add(bean.id)
//                    selectData.add(bean)
//                }
//            }
//            adapter.setDiffNewData(newData)
//        }
//        //从任务复制原始记录
//        binding.btnTaskCopy.setOnClickListener {
//            if (selectData.isEmpty()) {
//                Snackbar.make(binding.root, "您还没有选择任务。", Snackbar.LENGTH_SHORT).show()
//            } else {
//                val sameDeviceList = ArrayList<TaskRecordVO>()
//                for (i in selectData.indices) {
//                    if (selectData[0].equipmentKindCode != selectData[i].equipmentKindCode) {
//                        sameDeviceList.add(selectData[i])
//                    }
//                }
//                if (sameDeviceList.isEmpty()) {
//                    SweetAlertDialog(requireContext(), SweetAlertType.WARNING_TYPE)
//                        .setTitleText("请选择复制方式")
//                        .setConfirmButton("本单位复制") {
//                            TheRouter.build("/activity/batchCopy")
//                                .withString("unitName", unitName)
//                                .fillParams { bundle ->
//                                    bundle.putIntegerArrayList("ids", ids)
//                                }
//                                .withString("equipmentKindCode", selectData[0].equipmentKindCode)
//                                .withString("source", "Task")
//                                .withObject("templateObj", HashMap<String, Any>())
//                                .navigation()
//                            for (i in adapter.data.indices) {
//                                adapter.data[i].isSelected = false
//                                adapter.notifyItemChanged(i)
//                            }
//                            ids.clear()
//                            selectData.clear()
//                            it.dismissWithAnimation()
//                        }
//                        .setCancelButton("跨单位复制") {
//                            TheRouter.build("/activity/batchCopy")
//                                .withString("unitName", "")
//                                .fillParams { bundle ->
//                                    bundle.putIntegerArrayList("ids", ids)
//                                }
//                                .withString("equipmentKindCode", selectData[0].equipmentKindCode)
//                                .withString("source", "Task")
//                                .withObject("templateObj", HashMap<String, Any>())
//                                .navigation()
//                            for (i in adapter.data.indices) {
//                                adapter.data[i].isSelected = false
//                                adapter.notifyItemChanged(i)
//                            }
//                            ids.clear()
//                            selectData.clear()
//                            it.dismissWithAnimation()
//                        }
//                        .show()
//                } else {
//                    Snackbar.make(binding.root, "请选择相同种类的设备进行复制。", Snackbar.LENGTH_SHORT).show()
//                }
//            }
//        }
//        // 提交审核
//        binding.btnSubmitAudit.setOnClickListener {
//            if (ids.isEmpty()) {
//                Snackbar.make(binding.root, "您还没有选择原始记录。", Snackbar.LENGTH_SHORT).show()
//                return@setOnClickListener
//            } else {
//                if (selectData.any { select -> !select.circulationState && !select.contractSubmitState }) {
//                    Snackbar.make(binding.root, "存在未流转或没提交履约情况的原始记录。", Snackbar.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//                SweetAlertDialog(requireContext(), SweetAlertType.WARNING_TYPE)
//                    .setContentText("您确定要将原始记录提交至审核吗？")
//                    .setConfirmButton("确定") {
//                        viewModel.submitAudit(ids)
//                        binding.cbSelectAll.isChecked = false
//                        it.dismissWithAnimation()
//                    }
//                    .setCancelButton("取消") { it.dismissWithAnimation() }
//                    .show()
//            }
//        }
//        // 新建流转
//        binding.btnCirculation.setOnClickListener {
//            if (ids.isEmpty()) {
//                Snackbar.make(binding.root, "您还没有选择原始记录。", Snackbar.LENGTH_SHORT).show()
//            } else {
//                if (selectData.any { select -> !select.disabledState || select.circulationState }) {
//                    Snackbar.make(binding.root, "存在未办结或已流转的原始记录。", Snackbar.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//                // 所选原始记录的重启状态必须一致
//                if (selectData.any { select -> select.restartState != selectData[0].restartState }) {
//                    Snackbar.make(binding.root, "所选原始记录的重启状态必须一致。", Snackbar.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//                // 所选原始记录的设备种类代码必须一致
//                if (selectData.any { select -> select.equipmentKindCode != selectData[0].equipmentKindCode }) {
//                    Snackbar.make(binding.root, "所选原始记录的设备种类代码必须一致。", Snackbar.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//                val circulationDTO = CirculationDTO(
//                    null,
//                    ids.toSet(),
//                    selectData.map { it.recordNumber }.toSet(),
//                    selectData[0].jobNumber,
//                    shebeizhongleidaima = selectData[0].equipmentKindCode.toString(),
//                    restartState = selectData[0].restartState,
//                    dengjijiguan = ""
//                )
//                CreateCirculationDialogFragment.newInstance(circulationDTO).show(parentFragmentManager, "CreateCirculationDialogFragment")
//            }
//        }
//        // 取消流转
//        binding.btnCancelCirculation.setOnClickListener {
//            if (ids.isEmpty()) {
//                Snackbar.make(binding.root, "您还没有选择原始记录。", Snackbar.LENGTH_SHORT).show()
//            } else {
//                if (selectData.any { select -> !select.circulationState }) {
//                    Snackbar.make(binding.root, "存在未流转的原始记录。", Snackbar.LENGTH_SHORT).show()
//                    return@setOnClickListener
//                }
//                SweetAlertDialog(requireContext(), SweetAlertType.WARNING_TYPE)
//                    .setContentText("您确定要取消流转吗？")
//                    .setConfirmButton("确定") { sweet ->
//                        viewModel.cancelCirculation(ids.toSet())
//                            .autoDispose(this)
//                            .subscribe({
//                                sweet.dismissWithAnimation()
//                            }, {})
//                    }
//                    .setCancelButton("取消") { it.dismissWithAnimation() }
//                    .show()
//            }
//        }
//        //任务退回
//        binding.btnRollbackTask.setOnClickListener {
//            if (ids.isEmpty()) {
//                Snackbar.make(binding.root, "您还没有选择任务。", Snackbar.LENGTH_SHORT).show()
//            } else {
//                SweetAlertDialog(requireContext(), SweetAlertType.WARNING_TYPE)
//                    .setContentText("您确定要退回任务吗？")
//                    .setConfirmButton("确定") {
//                        viewModel.putRollbackTask(ids)
//                        it.dismissWithAnimation()
//                    }
//                    .setCancelButton("取消") { it.dismissWithAnimation() }
//                    .show()
//            }
//        }
//        //任务作废
//        binding.btnInvalidTask.setOnClickListener {
//            if (ids.isEmpty()) {
//                Snackbar.make(binding.root, "您还没有选择任务。", Snackbar.LENGTH_SHORT).show()
//            } else {
//                InvalidTaskDialogFragment.newInstance(ids).show(parentFragmentManager, "InvalidTaskDialogFragment")
//            }
//        }
//    }
//
//    private fun getData() {
//        viewModel.submitAuditSuccess.observe(viewLifecycleOwner) {
//            selectData.clear()
//            ids.clear()
//        }
//        viewModel.rollbackTaskSuccess.observe(viewLifecycleOwner) {
//            selectData.clear()
//            ids.clear()
//        }
//        viewModel.invalidTaskSuccess.observe(viewLifecycleOwner) {
//            selectData.clear()
//            ids.clear()
//        }
//        when (type) {
//            FlowStatus.TASK_TODO -> {
//                viewModel.toDoTaskData.observe(viewLifecycleOwner) {
//                    adapter.setList(it)
//                    if (it.isEmpty()) {
//                        adapter.setEmptyView(R.layout.layout_empty)
//                    }
//                }
//            }
//
//            FlowStatus.CREATE_RECORD -> {
//                viewModel.editRecordData.observe(viewLifecycleOwner) {
//                    ids.clear()
//                    selectData.clear()
//                    adapter.setList(it)
//                    if (it.isEmpty()) {
//                        adapter.setEmptyView(R.layout.layout_empty)
//                    }
//                }
//            }
//
//            else -> adapter.setEmptyView(R.layout.layout_empty)
//        }
//    }
//
//    /**
//     * 获取存储卡读取权限
//     * @param position Int 位置
//     */
//    private fun checkPermission(position: Int) {
//        val hasWriteExternalPermission =
//            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        if (hasWriteExternalPermission == PackageManager.PERMISSION_GRANTED) {
//            Snackbar.make(binding.root, "正在获取数据...", Snackbar.LENGTH_LONG).show()
//            viewModel.getRecordPdfUrl(adapter.data[position].id,"YSJL")
//                .autoDispose(AndroidLifecycleScopeProvider.from(this))
//                .subscribe({ url ->
//                    if (url.isEmpty()) {
//                        Snackbar.make(binding.root, "获取记录 pdf 失败，请联系客服。", Snackbar.LENGTH_LONG).show()
//                    } else {
//                        toPDFViewer(requireContext(), url)
//                    }
//                }, {})
//        } else {
//            SweetAlertDialog(requireContext(), SweetAlertType.WARNING_TYPE)
//                .setTitleText("需要获取平板的储存卡读取权限")
//                .setConfirmButton("确定") {
//                    //未授权，申请授权(从相册选择图片需要读取存储卡的权限)
//                    ActivityCompat.requestPermissions(
//                        requireActivity(),
//                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), READ_EXTERNAL_STORAGE
//                    )
//                    it.dismissWithAnimation()
//                }
//                .setCancelButton("取消") { it.dismissWithAnimation() }
//                .show()
//        }
//    }
//
//    /**
//     * 生成主原始记录
//     */
//    private fun generateOriginal(position: Int) {
//        TemplateListDialogFragment.newInstance(
//            TemplateDialogDTO(
//                adapter.data[position].kindCode,
//                adapter.data[position].inspectionCategory,
//                adapter.data[position].id,
//                adapter.data[position].useUnit,
//                adapter.data[position].recordNumber
//            )
//        ).show(requireActivity().supportFragmentManager, "dialog")
//    }
//
//    /**
//     * 添加无损和分项
//     */
//    private fun addChildItemRecord(position: Int) {
//        ChildItemTemplateFragment.newInstance(
//            MetadataInfo(
//                adapter.data[position].id,
//                adapter.data[position].categoryCode!!,
//                adapter.data[position].useUnit,
//                disabledState = false,
//                retreatState = false,
//                restartState = false,
//                adapter.data[position].isVolume,
//                kindCode = "",
//                adapter.data[position].recordNumber,
//                adapter.data[position].inspectionCategory
//            ),
//            adapter.data[position].templateId
//        ).show(requireActivity().supportFragmentManager, "childItemDialog")
//    }
//
//    /**
//     * 查看已有无损和分项
//     */
//    private fun seeChildItemRecord(position: Int) {
//        RecordItemFragment.newInstance(
//            MetadataInfo(
//                adapter.data[position].id,
//                adapter.data[position].categoryCode!!,
//                adapter.data[position].useUnit,
//                adapter.data[position].disabledState,
//                adapter.data[position].retreatState,
//                adapter.data[position].restartState,
//                adapter.data[position].isVolume,
//                adapter.data[position].kindCode,
//                adapter.data[position].recordNumber,
//                adapter.data[position].inspectionCategory
//            ),
//            adapter.data[position].templateId
//        ).show(requireActivity().supportFragmentManager, "childRecordItemDialog")
//    }
//
//    /**
//     * 删除原始记录
//     */
//    private fun deleteRecord(position: Int) {
//        val mViewModel by activityViewModels<RecordViewModel>()
//        if ((requireActivity().application as ATApplication).isOnline()) {
//            SweetAlertDialog(context, SweetAlertType.WARNING_TYPE)
//                .setContentText("您确定要删除吗？")
//                .setConfirmButton("确定") {
//                    it.dismissWithAnimation()
//                    mViewModel.deleteRecord(adapter.data[position].id)
//                    Toast.makeText(requireContext(), "删除中...", Toast.LENGTH_SHORT).show()
//                }
//                .setCancelButton("取消") { it.dismissWithAnimation() }
//                .show()
//        } else {
//            SweetAlertDialog(context, SweetAlertType.ERROR_TYPE)
//                .setContentText("请在联网的情况下删除！")
//                .show()
//        }
//    }
//
//    companion object {
//        /**
//         *
//         * @param type Int
//         * @param engineeringNo String 工程编号
//         * @param unitName String 单位名称
//         * @return TaskOriginalFragment
//         */
//        fun newInstance(@FlowStatus type: Int, engineeringNo: String, unitName: String): ChatMsgFragment {
//            val fragment = ChatMsgFragment()
//            fragment.arguments = Bundle().apply {
//                this.putInt("type", type)
//                this.putString("engineeringNo", engineeringNo)
//                this.putString("unitName", unitName)
//            }
//            return fragment
//        }
//    }
//}