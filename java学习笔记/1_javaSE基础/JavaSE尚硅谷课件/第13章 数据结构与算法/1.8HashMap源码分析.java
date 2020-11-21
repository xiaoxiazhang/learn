    public HashMap() {
	   //把加载因子，初始化为0.75
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }
	
	public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
	
	//虽然是JDK1.7的算法不同，但是仍然是为了干扰key对象的hashCode值，得到一个更加分散的hash值
	static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
	
	final V putVal(int hash, K key, V value, boolean onlyIfAbsent,boolean evict) {
        Node<K,V>[] tab; 
		Node<K,V> p; 
		int n, i;
		
		//tab就是table
		//如果table是null或者是一个长度为0的数组
		//用n记录了table的长度
        if ((tab = table) == null || (n = tab.length) == 0)
			//对table重新调整了一下大小，长度变为16，threshold = 12
            n = (tab = resize()).length;
		
		/*
		i = (n-1) & hash  = (table.length-1) & hash;
		p = table[i]的头结点
		如果p为null，说明table[i]还没存储过其他元素
		*/
        if ((p = tab[i = (n - 1) & hash]) == null)
			//直接创建一个Node结点放到table[i]中，新结点的next是null
            tab[i] = newNode(hash, key, value, null);
        else {//如果p不为空，说明table[i]下面有其他结点
            Node<K,V> e; K k;
			//第一个if,是判断 table[i]的头结点是否是和新的映射关系的key重复
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;//如果重复，用e记录
            else if (p instanceof TreeNode)
				//如果p是树结点，就在树中查找是否有重复的key，如果有重复的用e记录哪个重复的结点
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {//如果p下面是链表，就在链表中查找是否有重复的key，如果有用e记录哪个重复的结点
				//一边找，一边记录当前链表的结点的个数
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
						//当链表的结点的个数 >= TREEIFY_THRESHOLD（树化阈值） - 1
						//因为新结点还未加入，如果加入，就称为8个了
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
							//考虑树化
                            treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
			
			//如果e不为null，说明找到了重复的，就用新的value覆盖原来的value
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        ++modCount;
		
		//如果++size > threshold，说明要扩容
        if (++size > threshold)
            resize();
		
        afterNodeInsertion(evict);
        return null;
    }
	
	
	final Node<K,V>[] resize() {
        Node<K,V>[] oldTab = table;
		//用oldCap记录原来的table的长度
        int oldCap = (oldTab == null) ? 0 : oldTab.length;//如果原来的table是空的，原来的容量就是0，否则就是取原来table的长度
       
	   int oldThr = threshold;//用oldThr记录原来的阈值
	   
        int newCap, newThr = 0;
		//原来的Table的容量非0，相当于是对原来table的一个扩容操作
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
            newCap = DEFAULT_INITIAL_CAPACITY; //新数组的容量为默认初始化容量16
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);//新的阈值是 16 * 0.75 = 12
        }
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
            Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
			//创建了一个新的数组，长度为newCap
			
		//让table指向新的数组
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