package com.metaloom.common.jdbc.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;

import java.util.Date;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 元数据基本信息
 * </p>
 *
 * @author Your Name
 * @since Date
 */
@Data
@EqualsAndHashCode(of = {"cInstId"})
@TableName("t_mtd_md_inst")
public class TMtdMdInst implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 元数据ID
     */
    @TableId(value = "c_inst_id", type = IdType.INPUT)
    private String cInstId;

    /**
     * 元模型ID
     */
    @TableField("c_class_id")
    private String cClassId;

    /**
     * 上级元数据ID
     */
    @TableField("c_parent_id")
    private String cParentId;

    /**
     * 元数据代码
     */
    @TableField("c_inst_code")
    private String cInstCode;

    /**
     * 元数据名称
     */
    @TableField("c_inst_name")
    private String cInstName;

    /**
     * 元数据ID路径
     */
    @TableField("c_inst_path")
    private String cInstPath;

    /**
     * 元数据code路径
     */
    @TableField("c_inst_code_path")
    private String cInstCodePath;

    /**
     * 元数据小版本
     */
    @TableField("c_version_no")
    private Integer cVersionNo;

    /**
     * 生效时间
     */
    @TableField("c_start_time")
    private Date cStartTime;

    /**
     * 系统ID
     * ```
     */
    @TableField("c_sys_id")
    private String cSysId;

    /**
     * 归属部门
     */
    @TableField("c_dept_id")
    private String cDeptId;

    /**
     * 业务代码最后修改时间
     */
    @TableField("c_busi_time")
    private Date cBusiTime;

    /**
     * 是否推荐上架 01是
     */
    @TableField("c_register_recommend_flag")
    private String cRegisterRecommendFlag;

    /**
     * 推荐时间
     */
    @TableField("c_recommend_time")
    private Date cRecommendTime;

    /**
     * 0导入1整合
     */
    @TableField("c_src_type")
    private String cSrcType;
}