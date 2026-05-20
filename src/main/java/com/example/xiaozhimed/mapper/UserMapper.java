package com.example.xiaozhimed.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.xiaozhimed.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
