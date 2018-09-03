package cronapi.database;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;

import cronapi.RestClient;

public class TransactionManager {

	private static ThreadLocal<Map<EntityManagerFactory, EntityManager>> CACHE = new ThreadLocal<>();

	private static RepositoryUtil ru = (RepositoryUtil) ApplicationContextHolder.getContext().getBean("repositoryUtil");

	public static JpaRepository findRepository(Class domainClass) {
		ListableBeanFactory factory = (ListableBeanFactory) ApplicationContextHolder.getContext();
		Repositories repositories = new Repositories(factory);
		if (repositories.hasRepositoryFor(domainClass)) {
			return (JpaRepository) repositories.getRepositoryFor(domainClass).orElse(null);
		}

		return null;
	}

	public static EntityManager getEntityManager(Class domainClass) {
		Map<EntityManagerFactory, EntityManager> map = CACHE.get();
		if (map == null) {
			map = new HashMap<>();
			CACHE.set(map);
		}

		EntityManagerFactory factory = ru.getEntityManagerFactory(domainClass);
		EntityManager em = map.get(factory);

		if (em == null) {
			em = factory.createEntityManager();
			map.put(factory, em);
		}

		TenantService tenantService = RestClient.getRestClient().getTenantService();

		if (tenantService != null && tenantService.getContextIds() != null) {
			Set<String> keySet = tenantService.getContextIds().keySet();
			for (String key : keySet) {
				em.setProperty(key, tenantService.getId(key));
			}
		}

		return em;
	}

	public static EntityManagerFactory findEntityManagerFactory(Class domainClass) {
		return ru.getEntityManagerFactory(domainClass);
	}

	public static void commit(Class domainClass) {
		Map<EntityManagerFactory, EntityManager> map = CACHE.get();
		if (map != null) {
			EntityManagerFactory factory = findEntityManagerFactory(domainClass);
			if (factory != null) {
				EntityManager em = map.get(factory);
				if (em != null) {
					if (em.getTransaction().isActive())
						em.getTransaction().commit();
				}
			}
		}
	}

	public static void rollback(Class domainClass) {
		Map<EntityManagerFactory, EntityManager> map = CACHE.get();
		if (map != null) {
			EntityManagerFactory factory = findEntityManagerFactory(domainClass);
			if (factory != null) {
				EntityManager em = map.get(factory);
				if (em != null) {
					if (em.getTransaction().isActive())
						em.getTransaction().rollback();
				}
			}
		}
	}

	public static void close(Class domainClass) {
		Map<EntityManagerFactory, EntityManager> map = CACHE.get();
		if (map != null) {
			EntityManagerFactory factory = findEntityManagerFactory(domainClass);
			if (factory != null) {
				EntityManager em = map.get(factory);
				if (em != null) {
					em.close();
				}
			}
		}
	}

	public static void commit() {
		Map<EntityManagerFactory, EntityManager> map = CACHE.get();
		if (map != null) {
			for (EntityManager em : map.values()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().commit();
				}
			}
		}
	}

	public static void rollback() {
		Map<EntityManagerFactory, EntityManager> map = CACHE.get();
		if (map != null) {
			for (EntityManager em : map.values()) {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
			}
		}
	}

	public static void close() {
		Map<EntityManagerFactory, EntityManager> map = CACHE.get();
		if (map != null) {
			for (EntityManager em : map.values()) {
				if (em.isOpen())
					em.close();
			}
		}
	}

	public static void clear() {
		Map<EntityManagerFactory, EntityManager> map = CACHE.get();
		if (map != null) {
			for (EntityManager em : map.values()) {
				try {
					em.clear();
				} catch (Exception e) {
					//Abafa
				}
			}
			map.clear();
		}

		CACHE.set(null);
		CACHE.remove();
	}
}
