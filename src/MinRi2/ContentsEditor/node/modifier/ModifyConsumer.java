package MinRi2.ContentsEditor.node.modifier;

public interface ModifyConsumer<T>{
    boolean modified();

    /**
     * 获取数据的类型
     */
    Class<?> getDataType();

    /**
     * 检查输入数据是否合法
     */
    boolean checkValue(T value);


    /**
     * 获取当前或默认的数据
     */
    T getData();

    /**
     * 保存数据
     */
    void modify(T value);

    void reset();
}