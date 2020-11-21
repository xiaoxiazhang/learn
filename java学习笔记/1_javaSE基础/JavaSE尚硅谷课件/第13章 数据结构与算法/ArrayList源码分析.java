private class ArrayList<E>{	
	Object[] elementData;//���������洢ArrayList��Ԫ��
	private int size;//��¼ʵ�ʴ��Ԫ�صĸ���
	private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};//�����飬����Ϊ0������
	
	/*
	����Ŀ����飬���ܴ洢Ԫ�ء����������ǣ�������new ArrayList()����û�д洢Ԫ��ʱ��
	������Ҫ�������Ŀռ䡣��Ϊ���ʱ��û�д洢Ԫ�أ�Ҳ���ܲ��档
	���磺����ĳ������  public ArrayList select(String goodsName){������Ʒ���ƣ���ѯ���ϵ���Ʒ��Ϣ
							//....
							//����п���û���ҵ��������ֲ�ϣ������null���������п��ܴ���û�м��null���ᱨ��ָ���쳣��
							//�������������new ArrayList()����������ȷʵû��Ԫ�أ����ԾͲ�Ҫnew Object[10];�˷���
						}
	*/
	private static final int DEFAULT_CAPACITY = 10; //Ĭ������
	 private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
	public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }
	
	public boolean add(E e) {
		//���Ƿ���Ҫ����
        ensureCapacityInternal(size + 1);  // Increments modCount!!
		
		//����Ԫ�ش洢��elementData[size]λ�ã�����size��1
        elementData[size++] = e;
        return true;
    }
	
	private void ensureCapacityInternal(int minCapacity) {
		//�ж�elementData�Ƿ��ǿ����飬����ǣ���ζ���һ�δ���Ԫ��
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
			//minCapacity����С��Ҫ����
			//Math.max(DEFAULT_CAPACITY, minCapacity)����DEFAULT_CAPACITY��minCapacity�����ֵ
			//DEFAULT_CAPACITY��10
			//minCapacity���βΣ������������ʱ�����ʵ��ֵ�Ƕ��������Ƕ��١�
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }
	
	private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
		//minCapacity����С��Ҫ����
		//elementData.length����ǰ����ĳ���
		//if (minCapacity - elementData.length > 0) ��ζ�ŵ�ǰ���鲻������
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    }
	
	private void grow(int minCapacity) {
        // overflow-conscious code
		//oldCapacity��ԭ��������ĳ���
        int oldCapacity = elementData.length;
		//newCapacity��������ĳ��� = ԭ������ĳ��� + ԭ������ĳ��� /2; ��1.5��
        int newCapacity = oldCapacity + (oldCapacity >> 1);
		
		//minCapacity����С��Ҫ����
		//���1.5��������
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity; //������ĳ��ȾͰ�������Ҫ��������
		
		//������ĳ��� > ��������������͸���һ������������
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
		
        // minCapacity is usually close to size, so this is a win:
		//��elementData����Ԫ�ص��������У�������ĳ���ΪnewCapacity
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
	
	public void add(int index, E element) {
        rangeCheckForAdd(index);

		//���Ƿ���Ҫ����
        ensureCapacityInternal(size + 1);  // Increments modCount!!
		
		//��[index]�Լ������Ԫ�������ƶ�
        System.arraycopy(elementData, index, elementData, index + 1,size - index);
		
		//����Ԫ����ӵ� elementData[index]
        elementData[index] = element;
		//Ԫ�ظ�������
        size++;
    }
	
	private void rangeCheckForAdd(int index) {
		//elementDataʵ���Ѿ��洢[0,size-1]
		//�ɲ����λ��[0,size]
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
	
	
	public E remove(int index) {
        rangeCheck(index);//ɾ��ʱ���±���

        modCount++;
		
		//��oldValue��¼Ҫ��ɾ����[index]λ�õ�Ԫ�أ���Ϊ֮��Ҫ���ر�ɾ����Ԫ��
        E oldValue = elementData(index);

		//Ҫ�ƶ���Ԫ�صĸ���
        int numMoved = size - index - 1;
		//���Ҫ�ƶ���Ԫ�صĸ���>0���ٵ���System.arraycopy()�����ƶ��������0�Ͳ�������
		//�������Ҫɾ�����ǵ�ǰ�����[size-1]��Ԫ�أ���ζ�Ų���Ҫ�ƶ�����ô�Ͳ��õ���System.arraycopy����������Ҫ��ջ����ջ���˷�ʱ��
        if (numMoved > 0)
            System.arraycopy(elementData, index+1, elementData, index, numMoved);
		
		//��GC����������������elementData[size-1]λ�õ�����Ԫ��
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
                if (elementData[index] == null) {//���o�ǿգ���ô���ǿ�elementData��˭��null
                    fastRemove(index);
                    return true;
                }
        } else {
            for (int index = 0; index < size; index++)
                if (o.equals(elementData[index])) {//���o�ǿգ���ô��equals�Ƚϣ����ĸ����㣬������o.equals(xx)
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
        rangeCheck(index);//����±�

		//�ȼ�¼Ҫ���滻��[index]λ�õ�Ԫ��		
        E oldValue = elementData(index);
        elementData[index] = element;
        return oldValue;
    }
	
	public E get(int index) {
        rangeCheck(index);//����±�

        return elementData(index);
    }
	
	E elementData(int index) {
        return (E) elementData[index];
    }
	
	
	public Iterator<E> iterator() {
        return new Itr();
    }
	
	//ʵ����Iterator�ӿ�
	private class Itr implements Iterator<E> {
		//�α꣬��ǰ��������������̬�����ĸ�λ����
        int cursor;       // index of next element to return
		//��һ��������λ��
        int lastRet = -1; // index of last element returned; -1 if no such
		//���浥����
        int expectedModCount = modCount;

        public boolean hasNext() {
			//��ЧԪ�صķ�Χ[0,size-1]
            return cursor != size;
        }

        @SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
			
			//��i��¼��ǰ��������������̬�����ĸ�λ����
            int i = cursor;
			
			//������ж����Է��û���next()����֮ǰû�е���hasNext()����
            if (i >= size)
                throw new NoSuchElementException();
			
			//��һ��������¼�˵�ǰ��̬�����elementData
            Object[] elementData = ArrayList.this.elementData;
			
			//�Ͳ����й�
            if (i >= elementData.length)
                throw new ConcurrentModificationException();
			
			
            cursor = i + 1;//�´η��ʵ�Ԫ�ص��±�
            return (E) elementData[lastRet = i];//i�Ǳ��α�����������±�λ�ã���lastRet��¼����Ӧ��һ�ε���next������lastRet�����ϴε��±�
			//��������remove�������õ���
		}

        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
				//ɾ��������ոյ���next()����ȡ�ߵ�λ�õ�Ԫ��
                ArrayList.this.remove(lastRet);
                cursor = lastRet;//��Ϊɾ��Ԫ�أ�[cursor]�ᱻ��ǰ�ƶ�����������Ҫcursor = lastRet
                lastRet = -1;//��Ϊ[lastRet]λ�ñ�ɾ���ˣ�Ȼ�󲻴����ˣ������������remove()�ͻᱨ��
                expectedModCount = modCount;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }		
}	