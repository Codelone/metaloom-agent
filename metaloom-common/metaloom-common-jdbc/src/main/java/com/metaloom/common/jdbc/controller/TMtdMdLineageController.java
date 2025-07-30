package com.metaloom.common.jdbc.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.metaloom.common.jdbc.entity.TMtdMdLineage;
import com.metaloom.common.jdbc.service.TMtdMdLineageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 元数据血缘关系Controller
 */
@RestController
@RequestMapping("/api/lineage")
public class TMtdMdLineageController {

    @Autowired
    private TMtdMdLineageService lineageService;

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Page<TMtdMdLineage> page(@RequestParam(defaultValue = "1") Integer current,
                                     @RequestParam(defaultValue = "10") Integer size) {
        return lineageService.page(new Page<>(current, size));
    }

    /**
     * 查询列表
     */
    @GetMapping("/list")
    public List<TMtdMdLineage> list() {
        return lineageService.list();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/{id}")
    public TMtdMdLineage getById(@PathVariable String id) {
        return lineageService.getById(id);
    }

    /**
     * 根据源系统ID查询
     */
    @GetMapping("/source/{sourceId}")
    public List<TMtdMdLineage> getBySourceId(@PathVariable String sourceId) {
        return lineageService.list(new QueryWrapper<TMtdMdLineage>().eq("c_source_sys_id", sourceId));
    }

    /**
     * 根据目标系统ID查询
     */
    @GetMapping("/target/{targetId}")
    public List<TMtdMdLineage> getByTargetId(@PathVariable String targetId) {
        return lineageService.list(new QueryWrapper<TMtdMdLineage>().eq("c_target_sys_id", targetId));
    }

    /**
     * 新增
     */
    @PostMapping
    public boolean save(@RequestBody TMtdMdLineage entity) {
        return lineageService.save(entity);
    }

    /**
     * 修改
     */
    @PutMapping
    public boolean update(@RequestBody TMtdMdLineage entity) {
        return lineageService.updateById(entity);
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable String id) {
        return lineageService.removeById(id);
    }
} 