package com.lin.subagent.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

/**
 * 模拟获取景点信息工具
 */
public class ScenicSpotInfoTool {

    @Tool(name = "scenic_spot_info", description = "获取指定景点的信息")
    public String getScenicSpotInfo(
            @ToolParam(name = "spot", description = "景点名称") String spot) {
        System.out.println("=====执行getScenicSpotInfo工具=======");
        String info = """
                        景点内容
                        1. 五羊石像 (The Five-Ram Stone Sculpture)
                        地位： 广州的城市徽章和最著名的标志。
                        看点： 位于木壳岗上，由130多块花岗岩雕刻而成。主峰石像高10余米，体积约53立方米。传说周夷王时，有五位仙人骑着口含六束谷穗的五色羊降临此地，祝福广州永无饥荒。
                        推荐理由： 来广州必打卡之地，几乎所有广州的宣传画册上都有它的身影。
                        2. 镇海楼 (Zhenhai Tower) & 广州博物馆
                        地位： 中国历史最悠久的博物馆之一，也是广州现存最完好、最具气势的古代高层建筑。
                        看点：
                        建筑： 俗称“五层楼”，高28米，共5层，红墙绿瓦，俗称“五层楼”。
                        馆藏： 楼内设有广州博物馆，陈列着从秦汉到明清时期的文物，是了解广州2000多年历史的最佳窗口。
                        视野： 登楼远眺，可俯瞰整个越秀公园及广州市区景色。
                        3. 明代古城墙 (Ming Dynasty City Wall)
                        看点： 镇海楼东西两侧保留了约1100多米长的明初城墙。
                        体验： 游客可以登上城墙漫步，触摸斑驳的砖石，感受历史的沧桑。城墙上还有雉堞（垛口）和炮台遗迹。
                        4. 中山纪念碑 (Sun Yat-sen Memorial Monument)
                        位置： 越秀山山顶。
                        看点： 为纪念孙中山先生而建，碑身是花岗石砌成的方锥形建筑，高37米，共8层。碑身四面均有碑文，由国民党元老胡汉民等人题写。
                """;
        return spot + info;
    }

}
