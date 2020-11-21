
    public HashMap() {
		//DEFAULT_INITIAL_CAPACITY��Ĭ�ϵĳ�ʼ������ 16
		//DEFAULT_LOAD_FACTOR��Ĭ�ϼ������� 0.75F
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);//���ñ��������������
    }
	
	
	public HashMap(int initialCapacity, float loadFactor) {
		//����if���Ǽ�������β��Ƿ�Ϸ�
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +loadFactor);

		//ʵ�ʵļ������ӱ���ֵΪ0.75
        this.loadFactor = loadFactor;
		//��ֵ = 16; ��ֵ��ʼ��Ϊ16
        threshold = initialCapacity;
        init();
    }
	
	
	public V put(K key, V value) {
		//table�����飬�洢��ֵ�Ե����飬Ԫ�ص�����Entry���͡�
		//���HashMap��û����ӹ�Ԫ�أ�table����һ��������
        if (table == EMPTY_TABLE) {
            inflateTable(threshold);//��ֵ = 16; ��ֵ��ʼ��Ϊ16
			//��������ǿ����飬���ȱ�Ϊ16��threshold = capacity * loadFactor = 16 * 0.75 = 12
        }
		
		//HashMap����keyΪnull��Hashtable������
        if (key == null)//���keyΪnull�����⴦��
            return putForNullKey(value);
			
		//����key��hashֵ
        int hash = hash(key);
		//�����µ�ӳ���ϵ�Ĵ洢�±�table[i]
        int i = indexFor(hash, table.length);
		
		
		//��ȡ��table[i]��ͷ���
		//���ͷ��㲻���㣬�������ж�����Ľ��  e = e.next
        for (Entry<K,V> e = table[i]; e != null; e = e.next) {
            Object k;
			//���e.hash == hash ���� Ҫô��e.key ���µ�ӳ���ϵ��key��ַ��ͬ��equls��ͬ
			//˵��e��key���µ�ӳ���ϵ��key��ͬ
            if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
				//���µ�value����ԭ����value
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
		//����µ�ӳ���ϵ��table[i]��λ�ã���Ϊtable[i]��ͷ��㣬ԭ��table[i]������������ӵ���next��
        addEntry(hash, key, value, i);
        return null;
    }
	
	private void inflateTable(int toSize) {
        // Find a power of 2 >= toSize
		//�������ĳ��Ȳ���2��n�η�������Ϊ2��n�η�
        int capacity = roundUpToPowerOf2(toSize);

		//���¼�����ֵ = capacity * loadFactor;
        threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
		
		//table���½������� ������Ϊ capacity
        table = new Entry[capacity];
		
		//��ʱ��������hash�����й�
        initHashSeedAsNeeded(capacity);
    }
	
	private static int roundUpToPowerOf2(int number) {
        // assert number >= 0 : "number must be non-negative";
        return number >= MAXIMUM_CAPACITY
                ? MAXIMUM_CAPACITY
                : (number > 1) ? Integer.highestOneBit((number - 1) << 1) : 1;
				
		//Integer.highestOneBit((number - 1) << 1)��������������þ��ǰ�һ����2����η����ֱ�Ϊ2��n�η�������			
    }
	
	private V putForNullKey(V value) {
		//����forѭ�������ã�
		//(1)��ȡ������table[0]�ĵ�һ��Ԫ��e�����e��Ϊnull
		//(2)�ж�e��key�Ƿ�Ϊnull�����e.keyΪnull�������µ�value����ԭ����value
		//(3)e=e.next�������ж���һ�����
		//���еĲ���������table[0]�����
		//keyΪnull�ļ�ֵ�ԣ�һ���Ǵ洢��table[0]����ġ�
        for (Entry<K,V> e = table[0]; e != null; e = e.next) {
            if (e.key == null) {
                V oldValue = e.value;
                e.value = value;
                e.recordAccess(this);
                return oldValue;
            }
        }
        modCount++;
		
		//���µļ�ֵ��(null,value)�洢��table[0]������
        addEntry(0, null, value, 0);//hash=0,key=null,value=value,bucketIndex=0
        return null;
    }
	
	void addEntry(int hash, K key, V value, int bucketIndex) {
		//size��HashMap�����м�ֵ�Եĸ���  
		//size >= threshold���ﵽ��ֵ ���� table[bucketIndex]�ǿ�
		//ͬʱ���������Ļ����ͻ�����
        if ((size >= threshold) && (null != table[bucketIndex])) {
            resize(2 * table.length);//������table����Ϊԭ����2��
            hash = (null != key) ? hash(key) : 0; //��д����key��hashֵ
            bucketIndex = indexFor(hash, table.length);//���¼���[bucketIndex]
			/*
			Ϊʲô�������ݺ�Ҫ���¼����±ꣿ
			index = hash & table.length-1;  ���table.length���ˣ�����Ҫ���¼��� [index]
			*/
        }

        createEntry(hash, key, value, bucketIndex);
    }
	
	//������������ֱ��ʹ��key��hashCode()����Ľ���ģ����Ǻܶ�ʱ���û��Լ�ʵ�ֵ�hashCode()���Ǻܺ�
	//��ͻ����Ƚ����أ���������hashCode()�Ļ���������һЩ���ŵĲ�����ʹ��hashֵ����ɢ��
	//���hashֵ����ɢ����ô�洢��table�оͻ�����ȷֲ��������Ƕ���ĳ��table[index]
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
		//ȡ��[bucketIndex]λ�õ�Ԫ�أ���table[bucketIndex]��ͷ���
        Entry<K,V> e = table[bucketIndex];
		
		//table[bucketIndex]��ͷ����Ϊ�½��(key,value)��Entry����
		//ԭ��table[bucketIndex]�����������Ϊ�½���next
        table[bucketIndex] = new Entry<>(hash, key, value, e);
		//Ԫ�ظ�������
        size++;
    }
	
	//������ͣ�������������LinkedList�п�����Node��ֻ����һ����ʽ�Ľ��
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
		//��key��hashֵ�� ������ĳ���-1������õ��±꣬��Χ[0,table.length-1]��Χ��
        return h & (length-1);
    }