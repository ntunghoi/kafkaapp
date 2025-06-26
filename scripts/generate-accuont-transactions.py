import sys
import csv
import random
from datetime import datetime, timedelta
import uuid
import string
from schwifty import IBAN

# Configuration
NUM_TRANSACTIONS = 500 # 1000000
NUM_ACCOUNTS = 10 # 50000
USERS = [1, 2, 3, 4]
COUNTRY_CODES = ['FR', 'DE', 'GB', 'CH']
CURRENCIES_BY_COUNTRY_CODE = {
    'FR': 'EUR',
    'DE': 'EUR',
    'GB': 'GBP',
    'CH': 'CHF'
}
CURRENCIES = ['EUR', 'GBP', 'CHF']
START_DATE = datetime(2024, 1, 1) # datetime(2015, 1, 1)
END_DATE = datetime(2024, 1, 11)

# Currency typical amount ranges (in local currency)
CURRENCY_RANGES = {
    'USD': (1, 50000),
    'HKD': (10, 400000),
    'EUR': (1, 45000),
    'JPY': (100, 5000000),
    'GBP': (1, 40000),
    'CHF': (1, 50000)
}

# Transaction types and their probabilities
TRANSACTION_TYPES = [
    ('DEBIT', 0.55),
    ('CREDIT', 0.45)
]

# Transaction categories
DEBIT_CATEGORIES = [
    'ATM_WITHDRAWAL', 'PURCHASE', 'TRANSFER_OUT', 'BILL_PAYMENT',
    'ONLINE_PURCHASE', 'RESTAURANT', 'GROCERY', 'FUEL', 'INSURANCE',
    'LOAN_PAYMENT', 'SUBSCRIPTION', 'UTILITIES', 'RENT', 'MEDICAL'
]

CREDIT_CATEGORIES = [
    'SALARY', 'TRANSFER_IN', 'DEPOSIT', 'INTEREST', 'REFUND',
    'DIVIDEND', 'BONUS', 'FREELANCE', 'INVESTMENT_RETURN', 'GIFT'
]

# Merchant/Description patterns
MERCHANTS = [
    'Amazon', 'Walmart', 'Target', 'Starbucks', 'McDonald\'s',
    'Shell', 'BP', 'Exxon', 'Visa', 'Mastercard',
    'PayPal', 'Apple', 'Google', 'Microsoft', 'Netflix',
    'Spotify', 'Uber', 'Lyft', 'Airbnb', 'Hotels.com'
]

def generate_account_number():
    """Generate a realistic account number"""
    country_code = random.choice(COUNTRY_CODES)
    return IBAN.random(country_code)

def generate_transaction_id():
    """Generate a unique transaction ID"""
    return str(uuid.uuid4())[:8].upper()

def generate_random_date(start_date, end_date):
    """Generate a random date between start and end dates"""
    time_between = end_date - start_date
    days_between = time_between.days
    random_days = random.randrange(days_between)
    random_date = start_date + timedelta(days=random_days)

    # Add random time
    random_hour = random.randint(0, 23)
    random_minute = random.randint(0, 59)
    random_second = random.randint(0, 59)

    return random_date.replace(hour=random_hour, minute=random_minute, second=random_second)

def generate_amount(currency):
    """Generate realistic transaction amount based on currency"""
    min_amt, max_amt = CURRENCY_RANGES[currency]

    # Use weighted random to make smaller amounts more common
    weights = [0.4, 0.3, 0.2, 0.1]  # 40% small, 30% medium, 20% large, 10% very large
    ranges = [
        (min_amt, min_amt + (max_amt - min_amt) * 0.1),
        (min_amt + (max_amt - min_amt) * 0.1, min_amt + (max_amt - min_amt) * 0.3),
        (min_amt + (max_amt - min_amt) * 0.3, min_amt + (max_amt - min_amt) * 0.7),
        (min_amt + (max_amt - min_amt) * 0.7, max_amt)
    ]

    selected_range = random.choices(ranges, weights=weights)[0]
    amount = random.uniform(selected_range[0], selected_range[1])

    # Round based on currency (JPY has no decimals, others have 2)
    if currency == 'JPY':
        return round(amount)
    else:
        return round(amount, 2)

def generate_description(transaction_type, category):
    """Generate realistic transaction description"""
    if transaction_type == 'DEBIT':
        if category in ['PURCHASE', 'ONLINE_PURCHASE']:
            return f"{random.choice(MERCHANTS)} - {category}"
        elif category == 'ATM_WITHDRAWAL':
            return f"ATM WITHDRAWAL - {random.choice(['BRANCH', 'STREET', 'MALL'])}"
        elif category == 'TRANSFER_OUT':
            return f"TRANSFER TO ACCOUNT ***{random.randint(1000, 9999)}"
        else:
            return f"{category.replace('_', ' ').title()}"
    else:  # CREDIT
        if category == 'SALARY':
            return f"SALARY DEPOSIT - {random.choice(['COMPANY A', 'COMPANY B', 'COMPANY C'])}"
        elif category == 'TRANSFER_IN':
            return f"TRANSFER FROM ACCOUNT ***{random.randint(1000, 9999)}"
        elif category == 'INTEREST':
            return "INTEREST CREDIT"
        else:
            return f"{category.replace('_', ' ').title()}"

def generate_balance():
    """Generate a realistic account balance"""
    return round(random.uniform(100, 100000), 2)

# Generate account number with user
account_numbers = [generate_account_number() for _ in range(NUM_ACCOUNTS)]

print(f"Generating {format(NUM_TRANSACTIONS, ',')} bank transactions...")
print("This may take a few minutes...")

output_filename = f'bank_transactions_{NUM_TRANSACTIONS}.csv'
if len(sys.argv) > 1:
    output_filename = sys.argv[1]

# Generate transactions and write to CSV
with open(output_filename, 'w', newline='', encoding='utf-8') as csvfile:
    fieldnames = [
        'transaction_id', 'user_id', 'account_number', 'transaction_date',
        'transaction_type', 'amount', 'currency', 'category', 'description', 'balance_after',
        'reference_number', 'branch_code', 'channel'
    ]

    writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
    writer.writeheader()

    user_accounts = {}
    for i in range(NUM_TRANSACTIONS):
        # Select transaction type
        transaction_type = random.choices(
            [t[0] for t in TRANSACTION_TYPES],
            weights=[t[1] for t in TRANSACTION_TYPES]
        )[0]

        # Select category based on transaction type
        if transaction_type == 'DEBIT':
            category = random.choice(DEBIT_CATEGORIES)
        else:
            category = random.choice(CREDIT_CATEGORIES)

        # Select account
        account_number = random.choice(account_numbers)

        # Select currency and preferred currency
        country_code = IBAN(account_number).country_code
        currency = CURRENCIES_BY_COUNTRY_CODE[country_code] # country_code # random.choice(CURRENCIES)

        user_id = user_accounts.get(account_number)
        if user_id is None:
            user_id = random.choice(USERS)
            user_accounts[account_number] = user_id

        # Generate transaction data
        transaction = {
            'transaction_id': generate_transaction_id(),
            "user_id": user_id,
            'account_number': account_number,
            'transaction_date': generate_random_date(START_DATE, END_DATE).strftime('%Y-%m-%d %H:%M:%S'),
            'transaction_type': transaction_type,
            'amount': generate_amount(currency),
            'currency': currency,
            'category': category,
            'description': generate_description(transaction_type, category),
            'balance_after': generate_balance(),
            'reference_number': f"REF{random.randint(100000000, 999999999)}",
            'branch_code': f"BR{random.randint(100, 999)}",
            'channel': random.choice(['ONLINE', 'ATM', 'BRANCH', 'MOBILE', 'PHONE'])
        }

        writer.writerow(transaction)

        # Progress indicator
        if (i + 1) % 100000 == 0:
            print(f"Generated {i + 1:,} transactions...")

print("\nGeneration complete!")
print(f"File saved as: {output_filename}")
print(f"Total transactions: {NUM_TRANSACTIONS:,}")
print(f"Currencies included: {', '.join(CURRENCIES)}")
print(f"Date range: {START_DATE.strftime('%Y-%m-%d')} to {END_DATE.strftime('%Y-%m-%d')}")
print(f"Approximate file size: ~150-200 MB")

# Generate summary statistics
print("\n=== SUMMARY STATISTICS ===")
currency_counts = {currency: NUM_TRANSACTIONS // len(CURRENCIES) for currency in CURRENCIES}
for currency, count in currency_counts.items():
    print(f"{currency}: ~{count:,} transactions")

print(f"\nUnique accounts: ~{len(account_numbers):,}")
print(f"Transaction types: {len(DEBIT_CATEGORIES + CREDIT_CATEGORIES)} categories")
print(f"Channels: ONLINE, ATM, BRANCH, MOBILE, PHONE")
