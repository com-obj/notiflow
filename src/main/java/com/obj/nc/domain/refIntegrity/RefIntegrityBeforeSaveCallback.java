package com.obj.nc.domain.refIntegrity;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.conversion.MutableAggregateChange;
import org.springframework.data.relational.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

import com.obj.nc.Get;

@Component
@ConditionalOnProperty(name="nc.app.check-reference-integrity", havingValue = "true")
public class RefIntegrityBeforeSaveCallback implements BeforeSaveCallback<Persistable<UUID>> {

	@Override
	public Persistable<UUID> onBeforeSave(Persistable<UUID> entity,
			MutableAggregateChange<Persistable<UUID>> messageChange) {

		try {
			List<AccessibleObject> refClassMembers = getMembersWithReferences(entity);
			
			for (AccessibleObject refClassMember : refClassMembers) {
				EntityExistanceChecker<UUID> refChecker = getReferenceChecker(refClassMember);

				List<UUID> referenceIds = getReferenceIds(entity, refClassMember);

				for (UUID refId : referenceIds) {
					if (!refChecker.existsById(refId)) {
						throw new RuntimeException(new SQLIntegrityConstraintViolationException(
								entity.getClass() + "." + entity.getId() + " is referencing persistable via " + refClassMember
										+ " attribute and reference " + refId + " which cannot be found in the DB"));

					}
				}

			}
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		} 

		return entity;
	}

	public List<AccessibleObject> getMembersWithReferences(Persistable<UUID> entity) {
		List<Method> refGetters = MethodUtils.getMethodsListWithAnnotation(entity.getClass(), Reference.class);
		List<Field> refFields = FieldUtils.getFieldsListWithAnnotation(entity.getClass(), Reference.class);
		
		List<AccessibleObject> refClassMembers = new ArrayList<AccessibleObject>();
		refClassMembers.addAll(refGetters);
		refClassMembers.addAll(refFields);
		return refClassMembers;
	}

	public EntityExistanceChecker<UUID> getReferenceChecker(AccessibleObject refClassMember) {
		Class<? extends EntityExistanceChecker<UUID>> refCheckerClass = refClassMember.getAnnotation(Reference.class).value();
		EntityExistanceChecker<UUID> refChecker = Get.getApplicationContext().getBean(refCheckerClass);

		if (refChecker == null) {
			throw new RuntimeException("Could not find bean with type " + refCheckerClass.getName()
					+ ". This bean is configured as reference checker in Attribute " + refClassMember);
		}
		return refChecker;
	}

	public List<UUID> getReferenceIds(Persistable<UUID> entity, AccessibleObject refClassMember) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		List<UUID> referenceIds = new ArrayList<UUID>();

		Object reference;
		if (refClassMember instanceof Field) {
			Field refField = (Field)refClassMember;
		
			refField.setAccessible(true);
			reference = refField.get(entity);
		} else {
			Method refMethod = (Method)refClassMember;
			
			reference = refMethod.invoke(entity);
		}
		
		if (reference == null) {
			return referenceIds;
		}
		
		if (reference instanceof UUID) {
			referenceIds.add((UUID) reference);
		} 
		
		else if (reference instanceof UUID[]) {
			for (UUID refId : (UUID[]) reference) {
				if (refId == null) {
					throw new RuntimeException("Reference stored in " + refClassMember + " contains null reference");
				}
				
				referenceIds.add(refId);
			}
		} 
		
		else if (reference instanceof Iterable<?>) {
			Iterator<UUID> iter = ((Iterable<UUID>) reference).iterator();
			while (iter.hasNext()) {
				UUID refId = iter.next();
				
				if (refId == null) {
					throw new RuntimeException("Reference stored in " + refClassMember + " contains null reference");
				}
				
				referenceIds.add(refId);
			}
		} 
		
		else {
			throw new RuntimeException("Could not get UUID(s) from reference " + refClassMember
					+ ". Only UUID and Iterable<UUID> are supported.");
		}
		
		return referenceIds;
	}

}
