public class LinkedList<E>{
	int size = 0;
	Node<E> first;//ͷ���
	Node<E> last;//β���
	
	private static class Node<E> {
        E item; //������
        Node<E> next; //��һ�����ĵ�ַ
        Node<E> prev; //��һ�����ĵ�ַ

        Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.next = next;
            this.prev = prev;
        }
    }
	
	public boolean add(E e) {
        linkLast(e);//���ӵ����
        return true;
    }
	
	void linkLast(E e) {
		//last�ǵ�ǰ��������һ�����
        final Node<E> l = last;
		
		//�����½��
		//Node(Node<E> prev, E element, Node<E> next) ��
		//�½���prev�� ԭ����������һ�����l�������½���prevָ��ԭ�������һ�����
		//�½���next�� null
		//�½����������ǣ�e
        final Node<E> newNode = new Node<>(l, e, null);
		
		//�½��������������һ�����
        last = newNode;
		
		//l��ԭ����������һ�����
		//���l==null��˵��ԭ��������һ��������
        if (l == null)
			//first������ĵ�һ����㣬
			//��������½�㼴�ǵ�һ����㣬�������һ�����
            first = newNode;
        else//l==null��������˵��ԭ��������ǿ�
			//l��ԭ����������һ����㣬ԭ�������һ�����ָ�����½��
            l.next = newNode;

		//����������
		size++;
        modCount++;
    }
	
	
	public void add(int index, E element) {
        checkPositionIndex(index);//���index�Ƿ�Ϸ����Ϸ���λ��[0��size]

        if (index == size)//���ӵ����
            linkLast(element);
        else //���򣬾��ǲ��뵽�м�
            linkBefore(element, node(index));
    }
	
	private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
	private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }
	
	//�����������[index]λ�õĽ��
	Node<E> node(int index) {
        // assert isElementIndex(index);
		//index < (size >>1) ��  index < size/2��˵��λ���������ǰ���
        if (index < (size >> 1)) {
			//��ͷ��ʼ�ұȽϺ���
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {//����˵��λ��������ĺ���
			//�������ǰ�ұȽϺ���
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }
	
	//e����Ҫ�����Ľ�������
	//succ��[index]λ�õĽ��
	void linkBefore(E e, Node<E> succ) {
        // assert succ != null;
		//pred��[index]λ�õ�ǰһ�����
        final Node<E> pred = succ.prev;
		//�����½�㣬Node(Node<E> prev, E element, Node<E> next) 
		//�½���prevָ��[index]λ�õ�ǰһ�����
		//�½���nextָ��ԭ��[index]�Ľ��
        final Node<E> newNode = new Node<>(pred, e, succ);
		
		//ԭ��[index]λ�õĽ��  ��prevָ���½��
        succ.prev = newNode;
		
		//ԭ��[index]λ�õ�ǰһ�������null��˵��Ҫ�����λ����ͷ����λ��
        if (pred == null)
			//�½������µ�ͷ���
            first = newNode;
        else
			//����ԭ��[index]λ�õ�ǰһ������nextָ���½��
            pred.next = newNode;
			
        size++;
        modCount++;
    }
	
	
	public boolean remove(Object o) {
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {//����������㣬˵��x��Ҫ��ɾ���Ľ��
                    unlink(x);
                    return true;
                }
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {//����������㣬˵��x��Ҫ��ɾ���Ľ��
                    unlink(x);
                    return true;
                }
            }
        }
        return false;
    }
	
	//x��Ҫ��ɾ���Ľ��
	E unlink(Node<E> x) {
        // assert x != null;
        final E element = x.item;//��ɾ����������
        final Node<E> next = x.next;//��ɾ��������һ�����
        final Node<E> prev = x.prev;//��ɾ��������һ�����

		//��ɾ��������һ�������null��˵����ɾ������ǵ�һ�����
        if (prev == null) {
			//��һ������Ϊ��ɾ��������һ�����
            first = next;
        } else {
			//����ɾ��������һ����� ��next ָ�� ��ɾ��������һ�����
            prev.next = next;
			//��ɾ���������֮ǰ����һ�����Ͽ�
            x.prev = null;
        }

		//��ɾ��������һ�������null��˵����ɾ����������һ�����
        if (next == null) {
			//��������һ����㣬��ɱ�ɾ��������һ�����
            last = prev;
        } else {
			//���򣬱�ɾ��������һ������prev = ��ɾ��������һ�����
            next.prev = prev;
			//��ɾ���������֮ǰ����һ�����Ͽ�
            x.next = null;
        }

		//�ѱ�ɾ�����������ÿ�
        x.item = null;//����x.prev = null ,x.next == null, x.item==null���൱��x������Ͽ���ϵ������Ҳ�ÿգ����Գ��ױ�����
        
		//Ԫ�ظ�������
		size--;
        modCount++;
        return element;
    }
	
	public E remove(int index) {
        checkElementIndex(index);//���index�Ƿ�Ϸ����Ϸ���λ��[0,size-1]
        return unlink(node(index));
    }
	
	private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }
	
	public E get(int index) {
        checkElementIndex(index);
		//node()�����������[index]λ�õĽ��
        return node(index).item;
    }
}