    public HashMap() {
	   //�Ѽ������ӣ���ʼ��Ϊ0.75
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }
	
	public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
	
	//��Ȼ��JDK1.7���㷨��ͬ��������Ȼ��Ϊ�˸���key�����hashCodeֵ���õ�һ�����ӷ�ɢ��hashֵ
	static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
	
	final V putVal(int hash, K key, V value, boolean onlyIfAbsent,boolean evict) {
        Node<K,V>[] tab; 
		Node<K,V> p; 
		int n, i;
		
		//tab����table
		//���table��null������һ������Ϊ0������
		//��n��¼��table�ĳ���
        if ((tab = table) == null || (n = tab.length) == 0)
			//��table���µ�����һ�´�С�����ȱ�Ϊ16��threshold = 12
            n = (tab = resize()).length;
		
		/*
		i = (n-1) & hash  = (table.length-1) & hash;
		p = table[i]��ͷ���
		���pΪnull��˵��table[i]��û�洢������Ԫ��
		*/
        if ((p = tab[i = (n - 1) & hash]) == null)
			//ֱ�Ӵ���һ��Node���ŵ�table[i]�У��½���next��null
            tab[i] = newNode(hash, key, value, null);
        else {//���p��Ϊ�գ�˵��table[i]�������������
            Node<K,V> e; K k;
			//��һ��if,���ж� table[i]��ͷ����Ƿ��Ǻ��µ�ӳ���ϵ��key�ظ�
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;//����ظ�����e��¼
            else if (p instanceof TreeNode)
				//���p������㣬�������в����Ƿ����ظ���key��������ظ�����e��¼�ĸ��ظ��Ľ��
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {//���p�������������������в����Ƿ����ظ���key���������e��¼�ĸ��ظ��Ľ��
				//һ���ң�һ�߼�¼��ǰ����Ľ��ĸ���
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
						//������Ľ��ĸ��� >= TREEIFY_THRESHOLD��������ֵ�� - 1
						//��Ϊ�½�㻹δ���룬������룬�ͳ�Ϊ8����
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
							//��������
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
			
			//���e��Ϊnull��˵���ҵ����ظ��ģ������µ�value����ԭ����value
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
		
		//���++size > threshold��˵��Ҫ����
        if (++size > threshold)
            resize();
		
        afterNodeInsertion(evict);
        return null;
    }
	
	
	final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
		//��oldCap��¼ԭ����table�ĳ���
        int oldCap = (oldTab == null) ? 0 : oldTab.length;//���ԭ����table�ǿյģ�ԭ������������0���������ȡԭ��table�ĳ���
       
	   int oldThr = threshold;//��oldThr��¼ԭ������ֵ
	   
        int newCap, newThr = 0;
		//ԭ����Table��������0���൱���Ƕ�ԭ��table��һ�����ݲ���
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                     oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr;
        else {               // zero initial threshold signifies using defaults
            newCap = DEFAULT_INITIAL_CAPACITY; //�����������ΪĬ�ϳ�ʼ������16
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);//�µ���ֵ�� 16 * 0.75 = 12
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
			//������һ���µ����飬����ΪnewCap
			
		//��tableָ���µ�����
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node<K,V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    else if (e instanceof TreeNode)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else { // preserve order
                        Node<K,V> loHead = null, loTail = null;
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;
                        do {
                            next = e.next;
                            if ((e.hash & oldCap) == 0) {
                                if (loTail == null)
                                    loHead = e;
                                else
                                    loTail.next = e;
                                loTail = e;
                            }
                            else {
                                if (hiTail == null)
                                    hiHead = e;
                                else
                                    hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }