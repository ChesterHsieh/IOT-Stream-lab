import json
import time
import pika
from faker import Faker

fake = Faker()

def generate_fake_order():
    return {
        'orderId': fake.uuid4(),
        'userId': fake.random_int(min=1, max=1000),
        'productId': fake.random_int(min=1, max=1000),
        'quantity': fake.random_int(min=1, max=10),
        'price': round(fake.random_number(digits=5), 2),
        'orderDate': fake.date_time_this_year().isoformat()
    }

def main():
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host='rabbitmq'))
    channel = connection.channel()

    channel.queue_declare(queue='orders')

    while True:
        order = generate_fake_order()
        channel.basic_publish(exchange='',
                              routing_key='orders',
                              body=json.dumps(order))
        print(f"Order sent: {order}")
        time.sleep(1)

    connection.close()

if __name__ == '__main__':
    main()