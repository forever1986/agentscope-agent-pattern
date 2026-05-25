package com.lin.supervisor.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

/**
 * 模拟获取部门人员信息的工具
 */
public class OrgTools {

    @Tool(
            name = "get_org_employee_emails",
            description = "获取部门下所有员工电子邮箱。")
    public String getOrgEmployeeEmails(@ToolParam(name = "org", description = "部门名称") String org) {
        System.out.println("==调用getOrgEmployeeEmails工具,入参：" + org + "==");
        // 模拟获取组织信息
        if(org!=null&&org.contains("设计")){
            return "Tom@email.com, Allen@email.com, Jimmy@email.com";
        }else if(org!=null&&org.contains("开发")){
            return "Mark@email.com, Smith@email.com, Parker@email.com";
        }else {
            return "non@email.com";
        }
    }
}
