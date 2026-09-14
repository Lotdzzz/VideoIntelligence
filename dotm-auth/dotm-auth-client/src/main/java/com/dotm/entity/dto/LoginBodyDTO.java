package com.dotm.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author dotm
 */
@Data
public class LoginBodyDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

}
