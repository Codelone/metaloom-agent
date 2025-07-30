package com.metaloom.common.jdbc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

/**
 * 元数据血缘关系实体类
 */
@TableName("t_mtd_md_lineage")
public class TMtdMdLineage extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关系ID
     */
    @TableId("c_lineage_id")
    private String lineageId;

    /**
     * 目标元数据ID
     */
    @TableField("c_target_inst_id")
    private String targetInstId;

    /**
     * 目标元模型ID
     */
    @TableField("c_target_class_id")
    private String targetClassId;

    /**
     * 目标系统ID
     */
    @TableField("c_target_sys_id")
    private String targetSysId;

    /**
     * 来源元数据ID
     */
    @TableField("c_source_inst_id")
    private String sourceInstId;

    /**
     * 来源元模型ID
     */
    @TableField("c_source_class_id")
    private String sourceClassId;

    /**
     * 来源系统ID
     */
    @TableField("c_source_sys_id")
    private String sourceSysId;

    /**
     * 生效时间
     */
    @TableField("c_start_time")
    private LocalDateTime startTime;

    /**
     * 关系来源ID
     */
    @TableField("c_src_inst_id")
    private String srcInstId;

    /**
     * 关系来源类型
     */
    @TableField("c_src_type")
    private String srcType;

    /**
     * 来源id
     */
    @TableField("c_source_id")
    private String sourceId;

    /**
     * 来源路径
     */
    @TableField("c_source_inst_path")
    private String sourceInstPath;

    /**
     * 目标路径
     */
    @TableField("c_target_inst_path")
    private String targetInstPath;

    // Getters and Setters

    public String getLineageId() {
        return lineageId;
    }

    public void setLineageId(String lineageId) {
        this.lineageId = lineageId;
    }

    public String getTargetInstId() {
        return targetInstId;
    }

    public void setTargetInstId(String targetInstId) {
        this.targetInstId = targetInstId;
    }

    public String getTargetClassId() {
        return targetClassId;
    }

    public void setTargetClassId(String targetClassId) {
        this.targetClassId = targetClassId;
    }

    public String getTargetSysId() {
        return targetSysId;
    }

    public void setTargetSysId(String targetSysId) {
        this.targetSysId = targetSysId;
    }

    public String getSourceInstId() {
        return sourceInstId;
    }

    public void setSourceInstId(String sourceInstId) {
        this.sourceInstId = sourceInstId;
    }

    public String getSourceClassId() {
        return sourceClassId;
    }

    public void setSourceClassId(String sourceClassId) {
        this.sourceClassId = sourceClassId;
    }

    public String getSourceSysId() {
        return sourceSysId;
    }

    public void setSourceSysId(String sourceSysId) {
        this.sourceSysId = sourceSysId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getSrcInstId() {
        return srcInstId;
    }

    public void setSrcInstId(String srcInstId) {
        this.srcInstId = srcInstId;
    }

    public String getSrcType() {
        return srcType;
    }

    public void setSrcType(String srcType) {
        this.srcType = srcType;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceInstPath() {
        return sourceInstPath;
    }

    public void setSourceInstPath(String sourceInstPath) {
        this.sourceInstPath = sourceInstPath;
    }

    public String getTargetInstPath() {
        return targetInstPath;
    }

    public void setTargetInstPath(String targetInstPath) {
        this.targetInstPath = targetInstPath;
    }
} 