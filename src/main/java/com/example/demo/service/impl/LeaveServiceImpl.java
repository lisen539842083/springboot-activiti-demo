package com.example.demo.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dao.LeaveMapper;
import com.example.demo.entity.LeaveInfo;
import com.example.demo.service.TestLeaveService;
import com.example.demo.service.LeaveService;

@Service
public class LeaveServiceImpl implements LeaveService {

	@Autowired
	private TestLeaveService  testLeaveService;
	@Autowired
	private LeaveMapper leaveMapper;
	@Autowired
	private RuntimeService runtimeService;
	@Override
	public void addLeaveAInfo(String msg) {
		LeaveInfo leaveInfo = new LeaveInfo();
		leaveInfo.setLeaveMsg(msg);
		String id = UUID.randomUUID().toString();
		leaveInfo.setId(id);
		leaveInfo.setStatus("0");
		//新增一条记录至数据库中
		leaveMapper.insert(leaveInfo);
		//启动流程引擎
		testLeaveService.startProcess(id);
	}

	@Override
	public List<LeaveInfo> getByUserId(String userId) {
		ArrayList<LeaveInfo> leaveInfoList = new ArrayList<>();
		//这是查询的ACT_RU_TESK表
		List<Task> list = testLeaveService.findTaskByUserId(userId);
		for (Task task : list) {
			ProcessInstance result = runtimeService
									.createProcessInstanceQuery()
									.processInstanceId(task.getProcessInstanceId()).singleResult();
			//获得业务流程的bussinessKey 就是 业务表中申请信息的id
			String businessKey = result.getBusinessKey();
			LeaveInfo leaveInfo = leaveMapper.getById(businessKey);
			leaveInfo.setTaskId(task.getId());
			//这边并没有将taskid存入到业务表中
			leaveInfoList.add(leaveInfo);
		}
		return leaveInfoList;
	}

	@Override
	public void completeTaskByUser(String taskId, String userId, String audit) {
		testLeaveService.completeTaskByUser(taskId, userId, audit);
	}


	@Override
	public void claimTaskBytaskIdAndUserId(String taskId, String userId, String audit) {
		testLeaveService.claimTaskBytaskIdAndUserId(taskId, userId, audit);
	}


}
