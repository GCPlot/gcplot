package com.gcplot.repository;

import com.gcplot.Identifier;
import com.gcplot.model.role.Role;

import java.util.List;
import java.util.Optional;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/14/16
 */
public interface RolesRepository {

    List<Role> roles();

    Optional<Role> role(Identifier identifier);

    Role store(Role role);

    void delete(Role role);

}
