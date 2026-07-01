package com.ruoyi.batch.statistics.domain;

import java.io.Serializable;

/**
 * 账号类型分布
 *
 * @author ruoyi
 */
public class BatchAccountTypePie implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 类型名称 */
    private String name;

    /** 数量 */
    private Long value;

    public BatchAccountTypePie()
    {
    }

    public BatchAccountTypePie(String name, Long value)
    {
        this.name = name;
        this.value = value;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Long getValue()
    {
        return value;
    }

    public void setValue(Long value)
    {
        this.value = value;
    }
}
