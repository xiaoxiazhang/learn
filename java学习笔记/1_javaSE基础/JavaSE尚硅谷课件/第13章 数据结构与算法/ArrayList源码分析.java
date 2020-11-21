private class ArrayList<E>{	
	Object[] elementData;//数组用来存储ArrayList的元素
	private int size;//记录实际存的元素的个数
	private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};//空数组，长度为0的数组
	
	/*
	这里的空数组，不能存储元素。它的作用是，当我们new ArrayList()，并没有存储元素时，
	并不需要申请额外的空间。因为你此时还没有存储元素，也可能不存。
	例如：当你某个方法  public ArrayList select(String goodsName){根据商品名称，查询符合的商品信息
							//....
							//结果有可能没有找到，我们又不希望返回null，调用者有可能粗心没有检查null，会报空指针异常。
							//所以我这里给它new ArrayList()，但是里面确实没有元素，所以就不要new Object[10];浪费了
						}
	*/
	private static final int DEFAULT_CAPACITY = 10; //默认容量
	 private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
	public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }
	
	public boolean add(E e) {
		//看是否需要扩容
        ensureCapacityInternal(size + 1);  // Increments modCount!!
		
		//将新元素存储到elementData[size]位置，并且size加1
        elementData[size++] = e;
        return true;
    }
	
	private void ensureCapacityInternal(int minCapacity) {
		//判断elementData是否是空数组，如果是，意味着我还未存过元素
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
			//minCapacity：最小需要容量
			//Math.max(DEFAULT_CAPACITY, minCapacity)返回DEFAULT_CAPACITY和minCapacity中最大值
			//DEFAULT_CAPACITY：10
			//minCapacity是形参，调用这个方法时传入的实参值是多少它就是多少。
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }
	
	private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
		//minCapacity：最小需要容量
		//elementData.length：当前数组的长度
		//if (minCapacity - elementData.length > 0) 意味着当前数组不够用了
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
	
	private void grow(int minCapacity) {
        // overflow-conscious code
		//oldCapacity：原来的数组的长度
        int oldCapacity = elementData.length;
		//newCapacity：新数组的长度 = 原来数组的长度 + 原来数组的长度 /2; 即1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
		
		//minCapacity：最小需要容量
		//如果1.5倍还不够
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity; //新数组的长度就按照你需要的容量来
		
		//新数组的长度 > 最大数组容量，就给你一个最大的数组容
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
		
        // minCapacity is usually close to size, so this is a win:
		//从elementData复制元素到新数组中，新数组的长度为newCapacity
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
	
	public void add(int index, E element) {
        rangeCheckForAdd(index);

		//看是否需要扩容
        ensureCapacityInternal(size + 1);  // Increments modCount!!
		
		//将[index]以及后面的元素往右移动
        System.arraycopy(elementData, index, elementData, index + 1,size - index);
		
		//将新元素添加到 elementData[index]
        elementData[index] = element;
		//元素个数增加
        size++;
    }
	
	private void rangeCheckForAdd(int index) {
		//elementData实际已经存储[0,size-1]
		//可插入的位置[0,size]
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
	
	
	public E remove(int index) {
        rangeCheck(index);//删除时的下标检查

        modCount++;
		
		//用oldValue记录要被删除的[index]位置的元素，因为之后要返回被删除的元素
        E oldValue = elementData(index);

		//要移动的元素的个数
        int numMoved = size - index - 1;
		//如果要移动的元素的个数>0，再调用System.arraycopy()方法移动，如果是0就不调用了
		//如果我们要删除的是当前数组的[size-1]的元素，意味着不需要移动，那么就不用调用System.arraycopy方法，不需要入栈，出栈，浪费时间
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index, numMoved);
		
		//让GC垃圾回收器，回收elementData[size-1]位置的无用元素
        elementData[--size] = null; // clear to let GC do its work

        return oldValue;
    }
	private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
	
	public boolean remove(Object o) {
        if (o == null) {
            for (int index = 0; index < size; index++)
                if (elementData[index] == null) {//如果o是空，那么我们看elementData中谁是null
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {//如果o非空，那么用equals比较，看哪个满足，而且用o.equals(xx)
                    fastRemove(index);
                    return true;
                }
        }
        return false;
    }
	
	private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index,
                             numMoved);
        elementData[--size] = null; // clear to let GC do its work
    }
	
	
	public E set(int index, E element) {
        rangeCheck(index);//检查下标

		//先记录要被替换的[index]位置的元素		
        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }
	
	public E get(int index) {
        rangeCheck(index);//检查下标

        return elementData(index);
    }
	
	E elementData(int index) {
        return (E) elementData[index];
    }
	
	
	public Iterator<E> iterator() {
        return new Itr();
    }
	
	//实现了Iterator接口
	private class Itr implements Iterator<E> {
		//游标，当前迭代器遍历到动态数组哪个位置了
        int cursor;       // index of next element to return
		//上一个迭代的位置
        int lastRet = -1; // index of last element returned; -1 if no such
		//后面单独讲
        int expectedModCount = modCount;

        public boolean hasNext() {
			//有效元素的范围[0,size-1]
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
			
			//用i记录当前迭代器遍历到动态数组哪个位置了
            int i = cursor;
			
			//加这个判断是以防用户在next()方法之前没有调用hasNext()方法
            if (i >= size)
                throw new NoSuchElementException();
			
			//用一个变量记录了当前动态数组的elementData
            Object[] elementData = ArrayList.this.elementData;
			
			//和并发有关
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
			
			
            cursor = i + 1;//下次访问的元素的下标
            return (E) elementData[lastRet = i];//i是本次遍历的数组的下标位置，用lastRet记录，对应下一次调用next方法，lastRet就是上次的下标
			//变量是在remove方法中用到了
		}

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
				//删除的是你刚刚调用next()方法取走的位置的元素
                ArrayList.this.remove(lastRet);
                cursor = lastRet;//因为删除元素，[cursor]会被往前移动，所以这里要cursor = lastRet
                lastRet = -1;//因为[lastRet]位置被删除了，然后不存在了，如果连续调用remove()就会报错
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }		
}	