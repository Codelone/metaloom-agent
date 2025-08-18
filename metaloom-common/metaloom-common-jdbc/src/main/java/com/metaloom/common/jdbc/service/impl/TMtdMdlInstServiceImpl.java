package com.metaloom.common.jdbc.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.metaloom.common.jdbc.entity.TMtdMdInst;
import com.metaloom.common.jdbc.mapper.TMtdMdInstMapper;
import com.metaloom.common.jdbc.service.TMtdMdlInstService;
import org.springframework.stereotype.Service;


/**
 * 表级元数据下级字段信息实现类
 */

@Service
public class TMtdMdlInstServiceImpl extends ServiceImpl<TMtdMdInstMapper, TMtdMdInst> implements TMtdMdlInstService {
}