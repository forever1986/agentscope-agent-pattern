package com.lin.skill.advance.utils;

import com.mysql.cj.jdbc.MysqlDataSource;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.ClasspathSkillRepository;
import io.agentscope.core.skill.repository.mysql.MysqlSkillRepository;

import java.io.IOException;
import java.util.List;

public class SaveSkillToDB {

    public static void main(String[] args) throws IOException {

        // 数据库配置
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setServerName("localhost");       // 数据库地址
        dataSource.setPort(3306);                    // 端口
        dataSource.setDatabaseName("agentscope");          // 库名
        dataSource.setUser("root");                  // 用户名
        dataSource.setPassword("root");              // 密码

        // 创建从Mysql获取Skill的读取器
        MysqlSkillRepository mysqlSkillRepository = new MysqlSkillRepository(dataSource, true, true);

        // 创建从Classpath目录获取Skill的读取器
        ClasspathSkillRepository skillRepository = new ClasspathSkillRepository("skills/tools");
        // 将skill写入到Mysql中
        List<AgentSkill> skills = skillRepository.getAllSkills();
        mysqlSkillRepository.save(skills, true);

    }
}
