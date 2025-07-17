package org.pahappa.converter;


import org.pahappa.utils.PermissionType;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 * This converter allows JSF to correctly convert between a PermissionType enum
 * and its String representation. It is automatically applied to any component
 * whose value is bound to a PermissionType object or a collection of them.
 */
@FacesConverter(value = "permissionTypeConverter", forClass = PermissionType.class)
public class PermissionTypeConverter implements Converter<PermissionType> {

    @Override
    public PermissionType getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return PermissionType.valueOf(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, PermissionType value) {
        if (value == null) {
            return "";
        }
        return value.name();
    }
}