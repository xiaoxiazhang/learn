
    public HashMap() {
		//DEFAULT_INITIAL_CAPACITY：默认的初始化容量 16
		//DEFAULT_LOAD_FACTOR：默认加载因子 0.75F
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);//调用本类的其他构造器
    }
	
	
	public HashMap(int initialCapacity, float loadFactor) {
		//三个if都是检查插入的形参是否合法
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +loadFactor);

		//实际的加载因子被赋值为0.75
        this.loadFactor = loadFactor;
		//阈值 = 16; 阈值初始化为16
        threshold = initialCapacity;
        init();
    }
	
	
	public V put(K key, V value) {
		//table是数组，存储键值对的数组，元素的类型Entry类型。
		//如果HashMap还没有添加过元素，table就是一个空数组
        if (table == EMPTY_TABLE) {
            inflateTable(threshold);//阈值 = 16; 阈值初始化为16
			//如果数组是空数组，长度变为16，threshold = capacity * loadFactor = 16 * 0.75 = 12
        }
		
		//HashMap允许key为null，Hashtable不允许
        if (key == null)//如果key为null，特殊处理
            return putForNullKey(value);
			
		//计算key的hash值
        int hash = hash(key);
		//计算新的映射关系的存储下标table[i]
        int i = indexFor(hash, table.length);
		
		
		//先取出table[i]的头结点
		//如果头结点不满足，就依次判断下面的结点  e = e.next
        for (Entry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
			//如果e.hash == hash 并且 要么是e.key 和新的映射关系的key地址相同或equls相同
			//说明e的key与新的映射关系的key相同
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
				//用新的value覆盖原来的value
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
		//添加新的映射关系到table[i]的位置，作为table[i]的头结点，原来table[i]下面的链表连接到它next中
        addEntry(hash, key, value, i);
        return null;
    }
	
	private void inflateTable(int toSize) {
        // Find a power of 2 >= toSize
		//如果数组的长度不是2的n次方，纠正为2的n次方
        int capacity = roundUpToPowerOf2(toSize);

		//重新计算阈值 = capacity * loadFactor;
        threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
		
		//table重新建新数组 ，长度为 capacity
        table = new Entry[capacity];
		
		//暂时不管它，hash种子有关
        initHashSeedAsNeeded(capacity);
    }
	
	private static int roundUpToPowerOf2(int number) {
        // assert number >= 0 : "number must be non-negative";
        return number >= MAXIMUM_CAPACITY
                ? MAXIMUM_CAPACITY
                : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
				
		//Integer.highestOneBit((number - 1) << 1)：这个方法的作用就是把一个非2的你次方数字变为2的n次方的数字			
    }
	
	private V putForNullKey(V value) {
		//整个for循环的作用：
		//(1)先取出数组table[0]的第一个元素e，如果e不为null
		//(2)判断e的key是否为null，如果e.key为null，就用新的value覆盖原来的value
		//(3)e=e.next，继续判断下一个结点
		//所有的操作都是在table[0]下面的
		//key为null的键值对，一定是存储在table[0]下面的。
        for (Entry<K,V> e = table[0]; e != null; e = e.next) {
            if (e.key == null) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }
        modCount++;
		
		//把新的键值对(null,value)存储到table[0]的下面
        addEntry(0, null, value, 0);//hash=0,key=null,value=value,bucketIndex=0
        return null;
    }
	
	void addEntry(int hash, K key, V value, int bucketIndex) {
		//size：HashMap中所有键值对的个数  
		//size >= threshold，达到阈值 并且 table[bucketIndex]非空
		//同时满足它俩的话，就会扩容
        if ((size >= threshold) && (null != table[bucketIndex])) {
            resize(2 * table.length);//把数组table扩大为原来的2倍
            hash = (null != key) ? hash(key) : 0; //重写计算key的hash值
            bucketIndex = indexFor(hash, table.length);//重新计算[bucketIndex]
			/*
			为什么数组扩容后，要重新计算下标？
			index = hash & table.length-1;  如果table.length变了，就需要重新计算 [index]
			*/
        }

        createEntry(hash, key, value, bucketIndex);
    }
	
	//本来我们想着直接使用key的hashCode()计算的结果的，但是很多时候用户自己实现的hashCode()不是很好
	//冲突现象比较严重，所以他在hashCode()的基础上做了一些干扰的操作，使得hash值更分散。
	//如果hash值更分散，那么存储到table中就会更均匀分布，而不是都在某个table[index]
	final int hash(Object k) {
        int h = hashSeed;
        if (0 != h && k instanceof String) {
            return sun.misc.Hashing.stringHash32((String) k);
        }

        h ^= k.hashCode();

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
	
	void createEntry(int hash, K key, V value, int bucketIndex) {
		//取出[bucketIndex]位置的元素，即table[bucketIndex]的头结点
        Entry<K,V> e = table[bucketIndex];
		
		//table[bucketIndex]的头结点变为新结点(key,value)的Entry对象。
		//原来table[bucketIndex]下面的链表作为新结点的next
        table[bucketIndex] = new Entry<>(hash, key, value, e);
		//元素个数增加
        size++;
    }
	
	//结点类型，类似于我们在LinkedList中看到都Node，只是另一种形式的结点
	static class Entry<K,V> implements Map.Entry<K,V> {
        final K key;
        V value;
        Entry<K,V> next;
        int hash;

        /**
         * Creates new entry.
         */
        Entry(int h, K k, V v, Entry<K,V> n) {
            value = v;
            next = n;
            key = k;
            hash = h;
        }
	}
	
	
	static int indexFor(int h, int length) {
        // assert Integer.bitCount(length) == 1 : "length must be a non-zero power of 2";
		//用key的hash值， 和数组的长度-1做运算得到下标，范围[0,table.length-1]范围内
        return h & (length-1);
    }