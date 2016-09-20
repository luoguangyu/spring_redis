package com.ivan.redis.dao;

import com.ivan.redis.entity.User;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * Created by Ivan.Luo
 * Date: 9/18/2016 4:16 PM
 */
@Repository
public class UserDaoImpl extends AbstractBaseRedisDao<String, User> implements UserDao
{
	/**
	 * ����
	 *<br>------------------------------<br>
	 * @param user
	 * @return
	 */
	public boolean add(final User user) {
		boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				RedisSerializer<String> serializer = getRedisSerializer();
				byte[] key  = serializer.serialize(user.getId());
				byte[] name = serializer.serialize(user.getName());
				return connection.setNX(key, name);
			}
		});
		return result;
	}

	/**
	 * �������� ʹ��pipeline��ʽ
	 *<br>------------------------------<br>
	 *@param list
	 *@return
	 */
	public boolean add(final List<User> list) {
		Assert.notEmpty(list);
		boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				RedisSerializer<String> serializer = getRedisSerializer();
				for (User user : list) {
					byte[] key  = serializer.serialize(user.getId());
					byte[] name = serializer.serialize(user.getName());
					connection.setNX(key, name);
				}
				return true;
			}
		}, false, true);
		return result;
	}

	/**
	 * ɾ��
	 * <br>------------------------------<br>
	 * @param key
	 */
	public void delete(String key) {
		List<String> list = new ArrayList<String>();
		list.add(key);
		delete(list);
	}

	/**
	 * ɾ�����
	 * <br>------------------------------<br>
	 * @param keys
	 */
	public void delete(List<String> keys) {
		redisTemplate.delete(keys);
	}

	/**
	 * �޸�
	 * <br>------------------------------<br>
	 * @param user
	 * @return
	 */
	public boolean update(final User user) {
		String key = user.getId();
		if (get(key) == null) {
			throw new NullPointerException("�����в�����, key = " + key);
		}
		boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				RedisSerializer<String> serializer = getRedisSerializer();
				byte[] key  = serializer.serialize(user.getId());
				byte[] name = serializer.serialize(user.getName());
				connection.set(key, name);
				return true;
			}
		});
		return result;
	}

	/**
	 * ͨ��key��ȡ
	 * <br>------------------------------<br>
	 * @param keyId
	 * @return
	 */
	public User get(final String keyId) {
		User result = redisTemplate.execute(new RedisCallback<User>() {
			public User doInRedis(RedisConnection connection)
					throws DataAccessException {
				RedisSerializer<String> serializer = getRedisSerializer();
				byte[] key = serializer.serialize(keyId);
				byte[] value = connection.get(key);
				if (value == null) {
					return null;
				}
				String name = serializer.deserialize(value);
				return new User(keyId, name, null);
			}
		});
		return result;
	}
}